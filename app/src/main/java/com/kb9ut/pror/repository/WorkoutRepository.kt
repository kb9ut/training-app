package com.kb9ut.pror.domain.repository

import com.kb9ut.pror.data.local.dao.MuscleGroupVolume
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.data.local.entity.WorkoutExerciseEntity
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>
    fun getRecentWorkouts(limit: Int): Flow<List<WorkoutEntity>>
    fun getWorkoutsForDay(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntity>>
    fun getWorkoutDatesInRange(start: Long, end: Long): Flow<List<Long>>
    fun observeActiveWorkout(): Flow<WorkoutEntity?>
    suspend fun getActiveWorkout(): WorkoutEntity?
    suspend fun getWorkoutById(id: Long): WorkoutEntity?
    suspend fun startWorkout(name: String, routineId: Long? = null): Long
    suspend fun finishWorkout(workoutId: Long)
    suspend fun updateWorkout(workout: WorkoutEntity)
    suspend fun deleteWorkout(workoutId: Long)

    // Workout Exercises
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExerciseEntity>>
    suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long, orderIndex: Int, notes: String? = null): Long
    suspend fun removeExerciseFromWorkout(workoutExerciseId: Long)
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExerciseEntity)

    // Sets
    fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<WorkoutSetEntity>>
    suspend fun addSet(workoutExerciseId: Long, orderIndex: Int): Long
    suspend fun updateSet(set: WorkoutSetEntity)
    suspend fun deleteSet(setId: Long)
    suspend fun getPersonalBestByWeight(exerciseId: Long): WorkoutSetEntity?
    suspend fun getPersonalBestByReps(exerciseId: Long): WorkoutSetEntity?
    fun getCompletedSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>
    fun getCompletedSetsForExerciseChronological(exerciseId: Long): Flow<List<WorkoutSetEntity>>
    fun getVolumeByMuscleGroupSince(sinceMillis: Long): Flow<List<MuscleGroupVolume>>
}
