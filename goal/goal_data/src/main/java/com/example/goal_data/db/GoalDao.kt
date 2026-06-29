package com.example.goal_data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE category = :category")
    fun observe(category: String): Flow<List<GoalEntity>>

    @Upsert
    suspend fun upsertAll(goals: List<GoalEntity>)

    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE category = :category")
    suspend fun clearCategory(category: String)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Replace the whole category with a fresh remote snapshot (atomic). */
    @Transaction
    suspend fun replaceCategory(category: String, goals: List<GoalEntity>) {
        clearCategory(category)
        upsertAll(goals)
    }
}
