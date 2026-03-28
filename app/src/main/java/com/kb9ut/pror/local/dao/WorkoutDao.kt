package com.kb9ut.pror.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startedAt DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE finishedAt IS NULL LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE finishedAt IS NULL LIMIT 1")
    fun observeActiveWorkout(): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE startedAt BETWEEN :startOfDay AND :endOfDay ORDER BY startedAt DESC")
    fun getWorkoutsForDay(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE finishedAt IS NOT NULL ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentWorkouts(limit: Int): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity): Long

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Query("SELECT COUNT(*) FROM workouts WHERE startedAt BETWEEN :startOfMonth AND :endOfMonth")
    fun getWorkoutCountByMonth(startOfMonth: Long, endOfMonth: Long): Flow<Int>

    @Query("SELECT startedAt FROM workouts WHERE startedAt BETWEEN :start AND :end ORDER BY startedAt ASC")
    fun getWorkoutDatesInRange(start: Long, end: Long): Flow<List<Long>>

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
