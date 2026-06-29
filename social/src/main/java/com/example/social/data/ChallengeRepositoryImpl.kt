package com.example.social.data

import com.example.social.domain.model.Challenge
import com.example.social.domain.model.ChallengeMember
import com.example.social.domain.model.isActive
import com.example.social.domain.repository.ChallengeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject

class ChallengeRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ChallengeRepository {

    override val currentUid: String? get() = auth.currentUser?.uid

    private fun col() = db.collection("challenges")

    /**
     * Challenges I'm a participant in (accepted OR pending invite) — a single `participantUids`
     * query, so there's one listener and no flicker when membership transitions (e.g. accepting an
     * invite keeps me in participantUids; only memberUids/invitedUids change).
     */
    override fun observeMyChallenges(): Flow<List<Challenge>> {
        val me = currentUid ?: return flowOf(emptyList())
        return callbackFlow {
            val reg = col().whereArrayContains("participantUids", me).addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { it.toChallenge() }.orEmpty()
                    .sortedByDescending { it.startEpochDay }
                trySend(list)
            }
            awaitClose { reg.remove() }
        }
    }

    /** Public challenges anyone can browse/join (global). Limited; sorted client-side. */
    override fun observePublicChallenges(): Flow<List<Challenge>> {
        if (currentUid == null) return flowOf(emptyList())
        return callbackFlow {
            val reg = col().whereEqualTo("isPublic", true).limit(50).addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { it.toChallenge() }.orEmpty()
                    .sortedByDescending { it.memberUids.size }
                trySend(list)
            }
            awaitClose { reg.remove() }
        }
    }

    override suspend fun createChallenge(
        title: String,
        icon: String,
        metric: String,
        durationDays: Int,
        isPublic: Boolean,
        habitNames: List<String>,
        invited: List<ChallengeMember>,
    ): String {
        val me = currentUid ?: return ""
        val meDoc = db.collection("users").document(me).get().await()
        val meMember = ChallengeMember(
            uid = me,
            name = meDoc.getString("name").orEmpty(),
            username = meDoc.getString("username").orEmpty(),
            photoUrl = meDoc.getString("profileImageUrl").orEmpty(),
        )
        val invitedUids = invited.map { it.uid }.filter { it != me }.distinct()
        val ref = col().document()
        ref.set(
            mapOf(
                "title" to title,
                "icon" to icon,
                "metric" to metric,
                "durationDays" to durationDays,
                "startEpochDay" to LocalDate.now().toEpochDay(),
                "ownerUid" to me,
                "isPublic" to isPublic,
                "habitNames" to habitNames,
                "createdAt" to System.currentTimeMillis(),
                "memberUids" to listOf(me),
                "invitedUids" to invitedUids,
                "participantUids" to (listOf(me) + invitedUids),
                "members" to mapOf(me to mapOf("name" to meMember.name, "username" to meMember.username, "photoUrl" to meMember.photoUrl)),
                "progress" to mapOf(me to 0),
                "doneToday" to mapOf(me to 0L),
            )
        ).await()
        // Best-effort invite push (no-op until the push server is deployed).
        invitedUids.forEach { uid ->
            runCatching { PushApi.notify(auth, uid, type = "challengeInvite", extra = mapOf("title" to title)) }
        }
        return ref.id
    }

    override suspend fun joinChallenge(challengeId: String) {
        val me = currentUid ?: return
        val meDoc = db.collection("users").document(me).get().await()
        col().document(challengeId).update(
            "memberUids", FieldValue.arrayUnion(me),
            "participantUids", FieldValue.arrayUnion(me),
            "members.$me", mapOf(
                "name" to meDoc.getString("name").orEmpty(),
                "username" to meDoc.getString("username").orEmpty(),
                "photoUrl" to meDoc.getString("profileImageUrl").orEmpty(),
            ),
            "progress.$me", 0,
            "doneToday.$me", 0L,
        ).await()
    }

    override suspend fun acceptInvite(challengeId: String) {
        val me = currentUid ?: return
        val meDoc = db.collection("users").document(me).get().await()
        col().document(challengeId).update(
            "invitedUids", FieldValue.arrayRemove(me),
            "memberUids", FieldValue.arrayUnion(me),
            "members.$me", mapOf(
                "name" to meDoc.getString("name").orEmpty(),
                "username" to meDoc.getString("username").orEmpty(),
                "photoUrl" to meDoc.getString("profileImageUrl").orEmpty(),
            ),
            "progress.$me", 0,
            "doneToday.$me", 0L,
        ).await()
    }

    override suspend fun declineInvite(challengeId: String) {
        val me = currentUid ?: return
        col().document(challengeId).update(
            "invitedUids", FieldValue.arrayRemove(me),
            "participantUids", FieldValue.arrayRemove(me),
        ).await()
    }

    override suspend fun leaveChallenge(challengeId: String) {
        val me = currentUid ?: return
        col().document(challengeId).update(
            "memberUids", FieldValue.arrayRemove(me),
            "participantUids", FieldValue.arrayRemove(me),
            "members.$me", FieldValue.delete(),
            "progress.$me", FieldValue.delete(),
            "doneToday.$me", FieldValue.delete(),
        ).await()
    }

    override suspend fun myActiveChallenges(): List<Challenge> {
        val me = currentUid ?: return emptyList()
        return runCatching {
            col().whereArrayContains("memberUids", me).get().await()
                .documents.mapNotNull { it.toChallenge() }
                .filter { it.isActive() }
        }.getOrDefault(emptyList())
    }

    override suspend fun updateMyProgress(challengeId: String, progress: Int, lastDoneEpochDay: Long) {
        val me = currentUid ?: return
        runCatching {
            col().document(challengeId).update(
                "progress.$me", progress,
                "doneToday.$me", lastDoneEpochDay,
            ).await()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toChallenge(): Challenge? {
        val memberUids = (get("memberUids") as? List<String>) ?: return null
        val invitedUids = (get("invitedUids") as? List<String>).orEmpty()
        val membersMap = (get("members") as? Map<String, Map<String, Any?>>).orEmpty()
        val progressMap = (get("progress") as? Map<String, Any?>).orEmpty()
        val doneMap = (get("doneToday") as? Map<String, Any?>).orEmpty()
        val members = memberUids.map { uid ->
            val m = membersMap[uid].orEmpty()
            ChallengeMember(
                uid = uid,
                name = m["name"] as? String ?: "",
                username = m["username"] as? String ?: "",
                photoUrl = m["photoUrl"] as? String ?: "",
                progress = (progressMap[uid] as? Number)?.toInt() ?: 0,
                lastDoneEpochDay = (doneMap[uid] as? Number)?.toLong() ?: 0L,
            )
        }
        return Challenge(
            id = id,
            title = getString("title").orEmpty(),
            icon = getString("icon") ?: "spark",
            metric = getString("metric") ?: "any",
            durationDays = (getLong("durationDays") ?: 30L).toInt(),
            startEpochDay = getLong("startEpochDay") ?: 0L,
            ownerUid = getString("ownerUid").orEmpty(),
            isPublic = getBoolean("isPublic") ?: false,
            habitNames = (get("habitNames") as? List<String>).orEmpty(),
            memberUids = memberUids,
            invitedUids = invitedUids,
            members = members,
        )
    }
}
