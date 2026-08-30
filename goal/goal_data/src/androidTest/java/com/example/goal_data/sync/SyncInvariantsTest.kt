package com.example.goal_data.sync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.goal_data.db.GoalDao
import com.example.goal_data.db.GoalDatabase
import com.example.goal_data.mapper.toDomainGoal
import com.example.goal_data.mapper.toDto
import com.example.goal_data.mapper.toNewEntity
import com.example.goal_data.repository.GoalRepositoryImpl
import com.example.goal_domain.model.Goal
import com.example.goal_domain.sync.SyncOutcome
import com.example.goal_domain.usecase.HabitCompletion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * The invariants the offline sync design rests on. Each test names one, because when sync breaks in
 * the field it breaks as *one* of these — a completion that never uploaded, a habit that came back
 * from the dead, a row stuck dirty forever — and the failing test should say which.
 *
 * Instrumented rather than JVM: these lean on real SQLite transaction semantics.
 */
@RunWith(AndroidJUnit4::class)
class SyncInvariantsTest {

    private val userId = "user-1"
    private val today: LocalDate = LocalDate.now()

    private lateinit var db: GoalDatabase
    private lateinit var dao: GoalDao
    private lateinit var remote: FakeRemote
    private lateinit var scheduler: RecordingScheduler
    private lateinit var repo: GoalRepositoryImpl
    private lateinit var syncer: GoalSyncer

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, GoalDatabase::class.java).build()
        dao = db.goalDao()
        remote = FakeRemote()
        scheduler = RecordingScheduler()
        repo = GoalRepositoryImpl(remote, dao, db, scheduler)
        syncer = GoalSyncer(dao, remote)
    }

    @After
    fun tearDown() = db.close()

    private fun goal(id: String, title: String = "Read", challengeId: String = "") = Goal(
        id = id,
        category = "Habit",
        title = title,
        startDate = today.toString(),
        challengeId = challengeId,
    )

    /** A row both sides already agree on: settled at [revision], and present on the server. */
    private suspend fun seedSettled(g: Goal, revision: Long = 1, dao: GoalDao = this.dao) {
        dao.upsert(g.toNewEntity().copy(revision = revision, syncedRevision = revision))
        remote.docs[g.id] = g.toDto()
    }

    // ---------------------------------------------------------------- local mutation

    @Test
    fun mutation_increments_revision_and_requests_a_sync() = runBlocking {
        seedSettled(goal("g1"))

        repo.completeGoal(userId, "g1", today)

        val row = dao.getGoalById("g1")!!
        assertEquals("revision must move past syncedRevision to record the debt", 2L, row.revision)
        assertEquals("syncedRevision is only ever advanced by an upload", 1L, row.syncedRevision)
        assertEquals(1, scheduler.requests)
    }

    @Test
    fun no_op_mutation_does_not_increment_revision() = runBlocking {
        seedSettled(goal("g1", challengeId = "c1"))

        // Same value it already holds — the transform reports no change.
        repo.setChallengeLink(userId, "g1", "c1")

        val row = dao.getGoalById("g1")!!
        assertEquals(1L, row.revision)
        assertEquals("row is still clean, so nothing is owed", row.revision, row.syncedRevision)
        assertEquals("a no-op must not wake the sync worker", 0, scheduler.requests)
    }

    @Test
    fun an_edit_made_offline_is_kept_and_uploaded_later() = runBlocking {
        seedSettled(goal("g1", title = "Read"))
        remote.failSet = unavailable()

        repo.updateGoalDetails(userId, "g1", "Read 20 pages", "nightly", listOf(1, 3, 5), 7, "9:00 PM")

        assertEquals(
            "the edit form's write must land in the cache, not in a Firestore callback",
            "Read 20 pages", dao.getGoalById("g1")!!.title,
        )
        assertTrue(syncer.syncOnce(userId) is SyncOutcome.Retry)
        assertEquals("still offline, so the server has not been told", "Read", remote.docs["g1"]!!.title)

        remote.failSet = null
        assertEquals(SyncOutcome.Done, syncer.syncOnce(userId))
        assertEquals("Read 20 pages", remote.docs["g1"]!!.title)
        assertEquals(listOf(1, 3, 5), remote.docs["g1"]!!.selectedDays)
    }

    // ---------------------------------------------------------------- upload

    @Test
    fun successful_upload_advances_only_to_the_uploaded_revision() = runBlocking {
        dao.upsert(goal("g1").toNewEntity().copy(revision = 5, syncedRevision = 0))

        assertEquals(SyncOutcome.Done, syncer.syncOnce(userId))

        assertEquals(5L, dao.getGoalById("g1")!!.syncedRevision)

        // A late ack from an earlier, slower attempt must not walk the watermark backwards —
        // that would re-dirty a clean row and re-upload state the server already has.
        dao.markSynced("g1", 3)
        assertEquals(5L, dao.getGoalById("g1")!!.syncedRevision)
    }

    @Test
    fun mutation_during_an_upload_stays_dirty() = runBlocking {
        seedSettled(goal("g1"))
        repo.completeGoal(userId, "g1", today)          // revision 2, owed

        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        remote.setEntered = entered
        remote.setGate = release

        val pass = async(Dispatchers.IO) { syncer.syncOnce(userId) }
        entered.await()

        // The user taps again while revision 2 is still in flight. No transaction is held across
        // the network call, so this must succeed rather than block.
        repo.undoCompletion(userId, "g1", today)        // revision 3
        release.complete(Unit)

        val outcome = pass.await()
        val row = dao.getGoalById("g1")!!
        assertEquals(3L, row.revision)
        assertEquals("only revision 2 was ever uploaded", 2L, row.syncedRevision)
        assertEquals(
            "new work is not a failure — ask for another pass, do not burn the backoff curve",
            SyncOutcome.MoreWork, outcome,
        )
    }

    @Test
    // runBlocking<Unit>: this body ends in deleteDatabase(), which returns Boolean, and JUnit
    // rejects a test method that returns anything — the whole class fails to initialise.
    fun offline_mutation_survives_process_death() = runBlocking<Unit> {
        val name = "sync-durability-test.db"
        context.deleteDatabase(name)

        // --- session one: offline, user completes a habit -------------------------------------
        var session = Room.databaseBuilder(context, GoalDatabase::class.java, name).build()
        remote.failSet = unavailable()
        val sessionRepo = GoalRepositoryImpl(remote, session.goalDao(), session, scheduler)
        seedSettled(goal("g1"), dao = session.goalDao())
        sessionRepo.completeGoal(userId, "g1", today)
        session.close()                                  // process death

        // --- session two: fresh objects over the same file -------------------------------------
        session = Room.databaseBuilder(context, GoalDatabase::class.java, name).build()
        val recovered = session.goalDao().getUnsynced()
        assertEquals("the debt is recorded in the database, not in a coroutine", 1, recovered.size)
        assertEquals(2L, recovered.single().revision)
        assertTrue(HabitCompletion.isDoneOn(session.goalDao().getGoalById("g1")!!.toDomainGoal(), today))

        // Back online: the queued change uploads without the user doing anything.
        remote.failSet = null
        val outcome = GoalSyncer(session.goalDao(), remote).syncOnce(userId)
        assertEquals(SyncOutcome.Done, outcome)
        assertEquals(HabitCompletion.DONE, remote.docs["g1"]!!.progress[today.toString()])

        session.close()
        context.deleteDatabase(name)
    }

    // ---------------------------------------------------------------- deletes

    @Test
    fun tombstone_is_hidden_from_the_ui_but_still_queued() = runBlocking {
        seedSettled(goal("g1"))

        repo.deleteGoal(userId, "Habit", "g1")

        assertTrue("a deleted habit must vanish from the UI immediately",
            repo.observeGoals("Habit").first().isEmpty())
        val queued = dao.getUnsynced().single()
        assertTrue("but it is still work owed to the server", queued.pendingDelete)
    }

    @Test
    fun tombstone_survives_until_the_remote_delete_succeeds() = runBlocking {
        seedSettled(goal("g1"))
        repo.deleteGoal(userId, "Habit", "g1")

        remote.failDelete = unavailable()
        assertTrue(syncer.syncOnce(userId) is SyncOutcome.Retry)
        assertNotNull("the tombstone is the only record that the server still has this goal",
            dao.getGoalById("g1"))
        assertTrue(remote.docs.containsKey("g1"))

        remote.failDelete = null
        assertEquals(SyncOutcome.Done, syncer.syncOnce(userId))
        assertNull("purged only once the server agrees", dao.getGoalById("g1"))
        assertTrue(remote.docs.isEmpty())
    }

    // ---------------------------------------------------------------- refresh

    @Test
    fun refresh_cannot_overwrite_unsynced_local_state() = runBlocking {
        seedSettled(goal("g1"))
        repo.completeGoal(userId, "g1", today)           // local-only completion

        // The server still holds the pre-completion document — this snapshot predates our change.
        repo.refreshGoals(userId, "Habit")

        val row = dao.getGoalById("g1")!!
        assertTrue("the clear-and-reinsert this replaced lost exactly this",
            HabitCompletion.isDoneOn(dao.getGoalById("g1")!!.toDomainGoal(), today))
        assertEquals(2L, row.revision)
        assertEquals(1L, row.syncedRevision)
        assertTrue("and it is still queued for upload", dao.getUnsynced().isNotEmpty())
    }

    @Test
    fun refresh_takes_remote_state_for_clean_rows_without_resetting_the_counter() = runBlocking {
        seedSettled(goal("g1", title = "Read"), revision = 7)
        remote.docs["g1"] = goal("g1", title = "Read 20 pages").toDto()

        repo.refreshGoals(userId, "Habit")

        val row = dao.getGoalById("g1")!!
        assertEquals("a clean row has nothing to lose, so the remote wins", "Read 20 pages", row.title)
        assertEquals("the counter belongs to the row's lifetime and must not reset", 7L, row.revision)
        assertEquals("settled at its current revision", 7L, row.syncedRevision)
        assertTrue(dao.getUnsynced().isEmpty())
    }
}
