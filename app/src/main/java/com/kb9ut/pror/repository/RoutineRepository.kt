package com.kb9ut.pror.domain.repository

import com.kb9ut.pror.data.local.entity.RoutineEntity
import com.kb9ut.pror.data.local.entity.RoutineExerciseEntity
import com.kb9ut.pror.data.local.entity.RoutineSetTemplateEntity
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getAllRoutines(): Flow<List<RoutineEntity>>
    suspend fun getRoutineById(id: Long): RoutineEntity?
    suspend fun insertRoutine(routine: RoutineEntity): Long
    suspend fun updateRoutine(routine: RoutineEntity)
    suspend fun deleteRoutine(routineId: Long)
    suspend fun getRoutineExercises(routineId: Long): List<RoutineExerciseEntity>
    suspend fun insertRoutineExercise(exercise: RoutineExerciseEntity): Long
    suspend fun saveRoutineExercises(routineId: Long, exercises: List<RoutineExerciseEntity>)
    suspend fun getRoutineSetTemplates(routineExerciseId: Long): List<RoutineSetTemplateEntity>
    suspend fun saveRoutineSetTemplates(routineExerciseId: Long, templates: List<RoutineSetTemplateEntity>)
}
