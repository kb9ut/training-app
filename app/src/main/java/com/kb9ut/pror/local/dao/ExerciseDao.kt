package com.kb9ut.pror.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.MuscleGroup
import com.kb9ut.pror.data.local.entity.EquipmentCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE equipmentCategory = :category ORDER BY name ASC")
    fun getExercisesByEquipment(category: EquipmentCategory): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Delete
    suspend fun delete(exercise: ExerciseEntity)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getCount(): Int

    @Query("SELECT * FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getExerciseByName(name: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    suspend fun getExercisesByIds(ids: List<Long>): List<ExerciseEntity>

    @Query(
        """
        SELECT DISTINCT e.* FROM exercises e
        INNER JOIN workout_exercises we ON e.id = we.exerciseId
        INNER JOIN workout_sets ws ON we.id = ws.workoutExerciseId
        WHERE ws.isCompleted = 1
          AND ws.weightKg IS NOT NULL AND ws.reps IS NOT NULL
          AND ws.weightKg > 0 AND ws.reps > 0
        ORDER BY e.name ASC
        """
    )
    fun getExercisesWithData(): Flow<List<ExerciseEntity>>
}
