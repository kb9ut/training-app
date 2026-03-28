package com.kb9ut.pror.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Query("SELECT * FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY orderIndex ASC")
    fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY orderIndex ASC")
    suspend fun getSetsForWorkoutExerciseOnce(workoutExerciseId: Long): List<WorkoutSetEntity>

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId AND ws.isCompleted = 1
        ORDER BY ws.completedAt DESC
    """)
    fun getCompletedSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId AND ws.isCompleted = 1
          AND ws.reps IS NOT NULL AND ws.reps >= 1
        ORDER BY ws.weightKg DESC LIMIT 1
    """)
    suspend fun getPersonalBestByWeight(exerciseId: Long): WorkoutSetEntity?

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        WHERE we.exerciseId = :exerciseId AND ws.isCompleted = 1
        ORDER BY ws.reps DESC LIMIT 1
    """)
    suspend fun getPersonalBestByReps(exerciseId: Long): WorkoutSetEntity?

    @Query("SELECT * FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId AND isCompleted = 1 ORDER BY orderIndex ASC")
    suspend fun getCompletedSetsForWorkoutExerciseOnce(workoutExerciseId: Long): List<WorkoutSetEntity>

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE we.exerciseId = :exerciseId AND ws.isCompleted = 1 AND w.finishedAt IS NOT NULL
        ORDER BY ws.completedAt ASC
    """)
    fun getCompletedSetsForExerciseChronological(exerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("""
        SELECT e.muscleGroup AS muscleGroup, SUM(ws.weightKg * ws.reps) AS totalVolume
        FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        INNER JOIN exercises e ON we.exerciseId = e.id
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE ws.isCompleted = 1 AND w.finishedAt IS NOT NULL
        AND w.startedAt >= :sinceMillis
        GROUP BY e.muscleGroup
    """)
    fun getVolumeByMuscleGroupSince(sinceMillis: Long): Flow<List<MuscleGroupVolume>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: WorkoutSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sets: List<WorkoutSetEntity>): List<Long>

    @Query("UPDATE workout_sets SET rir = :rir WHERE id = :id")
    suspend fun updateRir(id: Long, rir: Int?)

    @Update
    suspend fun update(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
