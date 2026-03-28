package com.kb9ut.pror.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kb9ut.pror.data.local.entity.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {
    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExerciseEntity>>

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    suspend fun getExercisesForWorkoutOnce(workoutId: Long): List<WorkoutExerciseEntity>

    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getById(id: Long): WorkoutExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workoutExercise: WorkoutExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workoutExercises: List<WorkoutExerciseEntity>): List<Long>

    @Update
    suspend fun update(workoutExercise: WorkoutExerciseEntity)

    @Query("UPDATE workout_exercises SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?)

    @Query("DELETE FROM workout_exercises WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT we.* FROM workout_exercises we
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE we.exerciseId = :exerciseId AND w.finishedAt IS NOT NULL AND w.id != :currentWorkoutId
        ORDER BY w.startedAt DESC LIMIT 1
    """)
    suspend fun getLastWorkoutExercise(exerciseId: Long, currentWorkoutId: Long): WorkoutExerciseEntity?
}
