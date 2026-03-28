package com.kb9ut.pror.data.repository

import com.kb9ut.pror.data.local.dao.MuscleGroupVolume
import com.kb9ut.pror.data.local.dao.SetDao
import com.kb9ut.pror.data.local.dao.WorkoutDao
import com.kb9ut.pror.data.local.dao.WorkoutExerciseDao
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.data.local.entity.WorkoutExerciseEntity
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity
import com.kb9ut.pror.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao
) : WorkoutRepository {

    override fun getAllWorkouts(): Flow<List<WorkoutEntity>> =
        workoutDao.getAllWorkouts()

    override fun getRecentWorkouts(limit: Int): Flow<List<WorkoutEntity>> =
        workoutDao.getRecentWorkouts(limit)

    override fun getWorkoutsForDay(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntity>> =
        workoutDao.getWorkoutsForDay(startOfDay, endOfDay)

    override fun getWorkoutDatesInRange(start: Long, end: Long): Flow<List<Long>> =
        workoutDao.getWorkoutDatesInRange(start, end)

    override fun observeActiveWorkout(): Flow<WorkoutEntity?> =
        workoutDao.observeActiveWorkout()

    override suspend fun getActiveWorkout(): WorkoutEntity? =
        workoutDao.getActiveWorkout()

    override suspend fun getWorkoutById(id: Long): WorkoutEntity? =
        workoutDao.getWorkoutById(id)

    override suspend fun startWorkout(name: String, routineId: Long?): Long =
        workoutDao.insert(
            WorkoutEntity(
                name = name,
                startedAt = System.currentTimeMillis(),
                routineId = routineId
            )
        )

    override suspend fun finishWorkout(workoutId: Long) {
        val workout = workoutDao.getWorkoutById(workoutId) ?: return
        val now = System.currentTimeMillis()
        val duration = ((now - workout.startedAt) / 1000).toInt()
        workoutDao.update(
            workout.copy(
                finishedAt = now,
                durationSeconds = duration
            )
        )
    }

    override suspend fun updateWorkout(workout: WorkoutEntity) =
        workoutDao.update(workout)

    override suspend fun deleteWorkout(workoutId: Long) =
        workoutDao.deleteById(workoutId)

    override fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExerciseEntity>> =
        workoutExerciseDao.getExercisesForWorkout(workoutId)

    override suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long, orderIndex: Int, notes: String?): Long =
        workoutExerciseDao.insert(
            WorkoutExerciseEntity(
                workoutId = workoutId,
                exerciseId = exerciseId,
                orderIndex = orderIndex,
                notes = notes
            )
        )

    override suspend fun removeExerciseFromWorkout(workoutExerciseId: Long) =
        workoutExerciseDao.deleteById(workoutExerciseId)

    override suspend fun updateWorkoutExercise(workoutExercise: WorkoutExerciseEntity) =
        workoutExerciseDao.update(workoutExercise)

    override fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<WorkoutSetEntity>> =
        setDao.getSetsForWorkoutExercise(workoutExerciseId)

    override suspend fun addSet(workoutExerciseId: Long, orderIndex: Int): Long =
        setDao.insert(
            WorkoutSetEntity(
                workoutExerciseId = workoutExerciseId,
                orderIndex = orderIndex
            )
        )

    override suspend fun updateSet(set: WorkoutSetEntity) =
        setDao.update(set)

    override suspend fun deleteSet(setId: Long) =
        setDao.deleteById(setId)

    override suspend fun getPersonalBestByWeight(exerciseId: Long): WorkoutSetEntity? =
        setDao.getPersonalBestByWeight(exerciseId)

    override suspend fun getPersonalBestByReps(exerciseId: Long): WorkoutSetEntity? =
        setDao.getPersonalBestByReps(exerciseId)

    override fun getCompletedSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>> =
        setDao.getCompletedSetsForExercise(exerciseId)

    override fun getCompletedSetsForExerciseChronological(exerciseId: Long): Flow<List<WorkoutSetEntity>> =
        setDao.getCompletedSetsForExerciseChronological(exerciseId)

    override fun getVolumeByMuscleGroupSince(sinceMillis: Long): Flow<List<MuscleGroupVolume>> =
        setDao.getVolumeByMuscleGroupSince(sinceMillis)
}
