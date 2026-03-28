package com.kb9ut.pror.domain.repository

import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.EquipmentCategory
import com.kb9ut.pror.data.local.entity.MuscleGroup
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<ExerciseEntity>>
    fun getExercisesByEquipment(category: EquipmentCategory): Flow<List<ExerciseEntity>>
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>
    suspend fun getExerciseById(id: Long): ExerciseEntity?
    suspend fun getExercisesByIds(ids: List<Long>): Map<Long, ExerciseEntity>
    suspend fun insertExercise(exercise: ExerciseEntity): Long
    suspend fun updateExercise(exercise: ExerciseEntity)
    suspend fun deleteExercise(exercise: ExerciseEntity)
    suspend fun seedExercisesIfEmpty()
    suspend fun getExerciseByName(name: String): ExerciseEntity?
    fun getExercisesWithData(): Flow<List<ExerciseEntity>>
}
