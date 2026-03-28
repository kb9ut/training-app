package com.kb9ut.pror.data.repository

import com.kb9ut.pror.data.local.dao.RoutineDao
import com.kb9ut.pror.data.local.entity.RoutineEntity
import com.kb9ut.pror.data.local.entity.RoutineExerciseEntity
import com.kb9ut.pror.data.local.entity.RoutineSetTemplateEntity
import com.kb9ut.pror.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun getAllRoutines(): Flow<List<RoutineEntity>> =
        routineDao.getAllRoutines()

    override suspend fun getRoutineById(id: Long): RoutineEntity? =
        routineDao.getRoutineById(id)

    override suspend fun insertRoutine(routine: RoutineEntity): Long =
        routineDao.insertRoutine(routine)

    override suspend fun updateRoutine(routine: RoutineEntity) =
        routineDao.updateRoutine(routine)

    override suspend fun deleteRoutine(routineId: Long) =
        routineDao.deleteRoutineById(routineId)

    override suspend fun getRoutineExercises(routineId: Long): List<RoutineExerciseEntity> =
        routineDao.getRoutineExercises(routineId)

    override suspend fun insertRoutineExercise(exercise: RoutineExerciseEntity): Long =
        routineDao.insertRoutineExercise(exercise)

    override suspend fun saveRoutineExercises(routineId: Long, exercises: List<RoutineExerciseEntity>) {
        routineDao.deleteRoutineExercises(routineId)
        routineDao.insertRoutineExercises(exercises)
    }

    override suspend fun getRoutineSetTemplates(routineExerciseId: Long): List<RoutineSetTemplateEntity> =
        routineDao.getRoutineSetTemplates(routineExerciseId)

    override suspend fun saveRoutineSetTemplates(routineExerciseId: Long, templates: List<RoutineSetTemplateEntity>) {
        routineDao.deleteRoutineSetTemplates(routineExerciseId)
        routineDao.insertRoutineSetTemplates(templates)
    }
}
