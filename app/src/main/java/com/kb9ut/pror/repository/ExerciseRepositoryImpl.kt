package com.kb9ut.pror.data.repository

import com.kb9ut.pror.data.local.dao.ExerciseDao
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.EquipmentCategory
import com.kb9ut.pror.data.local.entity.MuscleGroup
import com.kb9ut.pror.data.local.seed.ExerciseSeedData
import com.kb9ut.pror.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<ExerciseEntity>> =
        exerciseDao.getAllExercises()

    override fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<ExerciseEntity>> =
        exerciseDao.getExercisesByMuscleGroup(muscleGroup)

    override fun getExercisesByEquipment(category: EquipmentCategory): Flow<List<ExerciseEntity>> =
        exerciseDao.getExercisesByEquipment(category)

    override fun searchExercises(query: String): Flow<List<ExerciseEntity>> =
        exerciseDao.searchExercises(query)

    override suspend fun getExerciseById(id: Long): ExerciseEntity? =
        exerciseDao.getExerciseById(id)

    override suspend fun insertExercise(exercise: ExerciseEntity): Long =
        exerciseDao.insert(exercise)

    override suspend fun updateExercise(exercise: ExerciseEntity) =
        exerciseDao.update(exercise)

    override suspend fun deleteExercise(exercise: ExerciseEntity) =
        exerciseDao.delete(exercise)

    override suspend fun seedExercisesIfEmpty() {
        if (exerciseDao.getCount() == 0) {
            exerciseDao.insertAll(ExerciseSeedData.getAll())
        }
    }

    override suspend fun getExerciseByName(name: String): ExerciseEntity? =
        exerciseDao.getExerciseByName(name)

    override suspend fun getExercisesByIds(ids: List<Long>): Map<Long, ExerciseEntity> =
        exerciseDao.getExercisesByIds(ids).associateBy { it.id }

    override fun getExercisesWithData(): Flow<List<ExerciseEntity>> =
        exerciseDao.getExercisesWithData()
}
