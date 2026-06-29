package com.example.social.data

import com.example.social.domain.model.FriendRequest
import com.example.social.domain.model.FriendState
import com.example.social.domain.model.FriendSummary
import com.example.social.domain.model.LeaderboardEntry
import com.example.social.domain.model.Nudge
import com.example.social.domain.model.UserSearchResult
import com.example.social.domain.repository.SocialRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firestore-only friend graph + accountability. Layout:
 *  - users/{uid}                       profile + denormalized activity summary (today/streak/weekly)
 *  - users/{uid}/friends/{friendUid}   accepted friends (written to both sides)
 *  - users/{uid}/nudges/{id}           incoming in-app nudges
 *  - friendRequests/{id}               pending requests { fromUid, toUid, status }
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SocialRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : SocialRepository {

    override val currentUid: String? get() = auth.currentUser?.uid

    // ---- Feed & leaderboard (friends' live summaries) ----

    override fun observeFeed(): Flow<List<FriendSummary>> {
        val me = currentUid ?: return flowOf(emptyList())
        return friendUidsFlow(me).flatMapLatest { uids ->
            if (uids.isEmpty()) flowOf(emptyList()) else usersSummaryFlow(uids)
        }
    }

    override fun observeLeaderboard(): Flow<List<LeaderboardEntry>> {
        val me = currentUid ?: return flowOf(emptyList())
        return friendUidsFlow(me).flatMapLatest { uids ->
            usersSummaryFlow((uids + me).distinct().take(30))
        }.map { summaries ->
            summaries.sortedByDescending { it.weeklyCompleted }
                .mapIndexed { i, s -> LeaderboardEntry(summary = s, rank = i + 1, isMe = s.uid == me) }
        }
    }

    private fun friendUidsFlow(me: String): Flow<List<String>> = callbackFlow {
        val reg = db.collection("users").document(me).collection("friends")
            .addSnapshotListener { snap, _ -> trySend(snap?.documents?.map { it.id }.orEmpty()) }
        awaitClose { reg.remove() }
    }

    private fun usersSummaryFlow(uids: List<String>): Flow<List<FriendSummary>> = callbackFlow {
        val safe = uids.take(30)
        if (safe.isEmpty()) { trySend(emptyList()); awaitClose { }; return@callbackFlow }
        val reg = db.collection("users")
            .whereIn(FieldPath.documentId(), safe)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.map { it.toSummary() }.orEmpty())
            }
        awaitClose { reg.remove() }
    }

    // ---- Requests ----

    override fun observeIncomingRequests(): Flow<List<FriendRequest>> {
        val me = currentUid ?: return flowOf(emptyList())
        return callbackFlow {
            val reg = db.collection("friendRequests")
                .whereEqualTo("toUid", me)
                .whereEqualTo("status", "pending")
                .addSnapshotListener { snap, _ ->
                    val list = snap?.documents?.map { doc ->
                        doc.toObject(FriendRequest::class.java)?.copy(id = doc.id) ?: FriendRequest(id = doc.id)
                    }.orEmpty()
                    trySend(list)
                }
            awaitClose { reg.remove() }
        }
    }

    override suspend fun searchUsers(query: String): List<UserSearchResult> {
        val me = currentUid ?: return emptyList()
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        val lower = q.lowercase()
        val cap = q.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        // Two prefix range queries (Firestore can't OR them) — by username (lowercased) and by name
        // (title-cased, since display names are usually capitalised). Merge + dedupe.
        val docs = runCatching {
            val byUsername = db.collection("users").orderBy("username")
                .startAt(lower).endAt(lower + "").limit(10).get().await().documents
            val byName = db.collection("users").orderBy("name")
                .startAt(cap).endAt(cap + "").limit(10).get().await().documents
            (byUsername + byName)
        }.getOrDefault(emptyList())
            .associateBy { it.id }.values
            .filter { it.id != me }
            .take(15)

        if (docs.isEmpty()) return emptyList()
        return resolveStates(me, docs)
    }

    override suspend fun suggestUsers(): List<UserSearchResult> {
        val me = currentUid ?: return emptyList()
        val docs = runCatching {
            db.collection("users").limit(30).get().await().documents
        }.getOrDefault(emptyList()).filter { it.id != me }
        if (docs.isEmpty()) return emptyList()
        return resolveStates(me, docs)
    }

    /** Resolve friend/pending state for a set of user docs in bulk (a few reads total). */
    private suspend fun resolveStates(
        me: String,
        docs: List<com.google.firebase.firestore.DocumentSnapshot>,
    ): List<UserSearchResult> {
        val friends = db.collection("users").document(me).collection("friends").get().await()
            .documents.map { it.id }.toSet()
        val outgoing = db.collection("friendRequests").whereEqualTo("fromUid", me).get().await()
            .documents.filter { it.getString("status") == "pending" }.mapNotNull { it.getString("toUid") }
        val incoming = db.collection("friendRequests").whereEqualTo("toUid", me).get().await()
            .documents.filter { it.getString("status") == "pending" }.mapNotNull { it.getString("fromUid") }
        val pending = (outgoing + incoming).toSet()

        return docs.map { doc ->
            val state = when {
                doc.id in friends -> FriendState.FRIENDS
                doc.id in pending -> FriendState.PENDING
                else -> FriendState.NONE
            }
            UserSearchResult(doc.toSummary(), state)
        }
    }

    override suspend fun sendFriendRequest(targetUid: String) {
        val me = currentUid ?: return
        if (targetUid == me) return
        val meDoc = db.collection("users").document(me).get().await()
        db.collection("friendRequests").add(
            mapOf(
                "fromUid" to me,
                "fromName" to meDoc.getString("name").orEmpty(),
                "fromUsername" to meDoc.getString("username").orEmpty(),
                "toUid" to targetUid,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    override suspend fun acceptRequest(request: FriendRequest) {
        val me = currentUid ?: return
        val meDoc = db.collection("users").document(me).get().await()
        val now = System.currentTimeMillis()
        val theirCard = mapOf("uid" to request.fromUid, "name" to request.fromName, "username" to request.fromUsername, "since" to now)
        val myCard = mapOf("uid" to me, "name" to meDoc.getString("name").orEmpty(), "username" to meDoc.getString("username").orEmpty(), "since" to now)
        db.collection("users").document(me).collection("friends").document(request.fromUid).set(theirCard).await()
        db.collection("users").document(request.fromUid).collection("friends").document(me).set(myCard).await()
        db.collection("friendRequests").document(request.id).delete().await()
    }

    override suspend fun declineRequest(request: FriendRequest) {
        db.collection("friendRequests").document(request.id).delete().await()
    }

    override suspend fun removeFriend(friendUid: String) {
        val me = currentUid ?: return
        db.collection("users").document(me).collection("friends").document(friendUid).delete().await()
        db.collection("users").document(friendUid).collection("friends").document(me).delete().await()
    }

    // ---- Nudges ----

    override fun observeUnseenNudges(): Flow<List<Nudge>> {
        val me = currentUid ?: return flowOf(emptyList())
        return callbackFlow {
            val reg = db.collection("users").document(me).collection("nudges")
                .whereEqualTo("seen", false)
                .addSnapshotListener { snap, _ ->
                    val list = snap?.documents?.map { doc ->
                        doc.toObject(Nudge::class.java)?.copy(id = doc.id) ?: Nudge(id = doc.id)
                    }.orEmpty()
                    trySend(list)
                }
            awaitClose { reg.remove() }
        }
    }

    override fun observeRecentNudges(): Flow<List<Nudge>> {
        val me = currentUid ?: return flowOf(emptyList())
        return callbackFlow {
            val reg = db.collection("users").document(me).collection("nudges")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(30)
                .addSnapshotListener { snap, _ ->
                    val list = snap?.documents?.map { doc ->
                        doc.toObject(Nudge::class.java)?.copy(id = doc.id) ?: Nudge(id = doc.id)
                    }.orEmpty()
                    trySend(list)
                }
            awaitClose { reg.remove() }
        }
    }

    override suspend fun sendNudge(toUid: String) {
        val me = currentUid ?: return
        val meDoc = db.collection("users").document(me).get().await()
        db.collection("users").document(toUid).collection("nudges").add(
            mapOf(
                "fromUid" to me,
                "fromName" to meDoc.getString("name").orEmpty(),
                "type" to "cheer",
                "createdAt" to System.currentTimeMillis(),
                "seen" to false,
            )
        ).await()
        // Best-effort push (no-op until PushConfig.ENDPOINT is set). The in-app nudge above is the
        // source of truth; a failed/absent push never blocks it.
        runCatching { PushApi.notify(auth, toUid, type = "nudge") }
    }

    override suspend fun markNudgesSeen() {
        val me = currentUid ?: return
        val unseen = db.collection("users").document(me).collection("nudges")
            .whereEqualTo("seen", false).get().await()
        if (unseen.isEmpty) return
        val batch = db.batch()
        unseen.documents.forEach { batch.update(it.reference, "seen", true) }
        batch.commit().await()
    }

    // ---- Activity summary ----

    override suspend fun updateMyActivity(
        todayDone: Int,
        todayTotal: Int,
        currentStreak: Int,
        weeklyCompleted: Int,
        lastWeekCompleted: Int,
        weeklyConsistency: Int,
        level: Int,
        bestStreak: Int,
        totalCompleted: Int,
    ) {
        val me = currentUid ?: return
        db.collection("users").document(me).set(
            mapOf(
                "todayDone" to todayDone,
                "todayTotal" to todayTotal,
                "currentStreak" to currentStreak,
                "weeklyCompleted" to weeklyCompleted,
                "lastWeekCompleted" to lastWeekCompleted,
                "weeklyConsistency" to weeklyConsistency,
                "level" to level,
                "bestStreak" to bestStreak,
                "totalCompleted" to totalCompleted,
                "activityUpdatedAt" to System.currentTimeMillis(),
            ),
            SetOptions.merge(),
        ).await()
    }

    // ---- helpers ----

    private fun com.google.firebase.firestore.DocumentSnapshot.toSummary() = FriendSummary(
        uid = id,
        name = getString("name").orEmpty(),
        username = getString("username").orEmpty(),
        photoUrl = getString("profileImageUrl").orEmpty(),
        currentStreak = (getLong("currentStreak") ?: 0L).toInt(),
        todayDone = (getLong("todayDone") ?: 0L).toInt(),
        todayTotal = (getLong("todayTotal") ?: 0L).toInt(),
        weeklyCompleted = (getLong("weeklyCompleted") ?: 0L).toInt(),
        lastWeekCompleted = (getLong("lastWeekCompleted") ?: 0L).toInt(),
        weeklyConsistency = (getLong("weeklyConsistency") ?: 0L).toInt(),
        level = (getLong("level") ?: 1L).toInt(),
        bestStreak = (getLong("bestStreak") ?: 0L).toInt(),
        totalCompleted = (getLong("totalCompleted") ?: 0L).toInt(),
    )
}
