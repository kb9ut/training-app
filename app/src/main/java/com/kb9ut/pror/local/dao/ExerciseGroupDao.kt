package com.kb9ut.pror.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kb9ut.pror.data.local.entity.ExerciseGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: ExerciseGroupEntity): Long

    @Query("SELECT * FROM exercise_groups WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    fun getGroupsForWorkout(workoutId: Long): Flow<List<ExerciseGroupEntity>>

    @Query("SELECT * FROM exercise_groups WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    suspend fun getGroupsForWorkoutOnce(workoutId: Long): List<ExerciseGroupEntity>

    @Query("SELECT * FROM exercise_groups WHERE id = :id")
    suspend fun getById(id: Long): ExerciseGroupEntity?

    @Query("DELETE FROM exercise_groups WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM workout_exercises WHERE groupId = :groupId")
    suspend fun getExerciseCountInGroup(groupId: Long): Int
}
