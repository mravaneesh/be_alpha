package com.example.goal_data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    /** Reads never show tombstoned rows — a pending delete is already gone as far as the UI cares. */
    @Query("SELECT * FROM goals WHERE category = :category AND pendingDelete = 0")
    fun observe(category: String): Flow<List<GoalEntity>>

    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    suspend fun getGoalById(goalId: String): GoalEntity?

    /** Every row, tombstones included, so a merge can see what it is about to overwrite. */
    @Query("SELECT * FROM goals WHERE category = :category")
    suspend fun getByCategory(category: String): List<GoalEntity>

    /** The sync queue. Tombstones qualify: a delete the remote has not been told about is work owed. */
    @Query("SELECT * FROM goals WHERE revision != syncedRevision")
    suspend fun getUnsynced(): List<GoalEntity>

    /**
     * Advance-only. The `syncedRevision < :uploaded` guard is what makes a late-landing ack from a
     * slow retry harmless: it can never walk the watermark backwards and re-dirty a clean row.
     */
    @Query("UPDATE goals SET syncedRevision = :uploaded WHERE id = :id AND syncedRevision < :uploaded")
    suspend fun markSynced(id: String, uploaded: Long)

    /** Soft delete: the row stays until the remote has been told, so the delete survives a crash. */
    @Query("UPDATE goals SET pendingDelete = 1, revision = revision + 1 WHERE id = :id")
    suspend fun markPendingDelete(id: String)

    /** Only ever removes a row we already told the server about. */
    @Query("DELETE FROM goals WHERE id = :id AND pendingDelete = 1")
    suspend fun purgeTombstone(id: String)
}
