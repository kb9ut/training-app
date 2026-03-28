package com.kb9ut.pror.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kb9ut.pror.data.local.entity.RoutineEntity
import com.kb9ut.pror.data.local.entity.RoutineExerciseEntity
import com.kb9ut.pror.data.local.entity.RoutineSetTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY lastUsedAt DESC, createdAt DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getRoutineExercises(routineId: Long): List<RoutineExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(exercise: RoutineExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(exercises: List<RoutineExerciseEntity>): List<Long>

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteRoutineExercises(routineId: Long)

    @Query("SELECT * FROM routine_set_templates WHERE routineExerciseId = :routineExerciseId ORDER BY orderIndex ASC")
    suspend fun getRoutineSetTemplates(routineExerciseId: Long): List<RoutineSetTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineSetTemplates(templates: List<RoutineSetTemplateEntity>): List<Long>

    @Query("DELETE FROM routine_set_templates WHERE routineExerciseId = :routineExerciseId")
    suspend fun deleteRoutineSetTemplates(routineExerciseId: Long)

    @Query("UPDATE routine_exercises SET notes = :notes WHERE routineId = :routineId AND exerciseId = :exerciseId")
    suspend fun updateRoutineExerciseNotes(routineId: Long, exerciseId: Long, notes: String?)
}
