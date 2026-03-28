package com.kb9ut.pror.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kb9ut.pror.data.local.entity.BodyWeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {
    @Query("SELECT * FROM body_weight ORDER BY recordedAt DESC")
    fun getAllBodyWeights(): Flow<List<BodyWeightEntity>>

    @Query("SELECT * FROM body_weight ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestBodyWeight(): BodyWeightEntity?

    @Query("SELECT * FROM body_weight WHERE recordedAt BETWEEN :startTime AND :endTime ORDER BY recordedAt ASC")
    fun getBodyWeightsInRange(startTime: Long, endTime: Long): Flow<List<BodyWeightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bodyWeight: BodyWeightEntity): Long

    @Update
    suspend fun update(bodyWeight: BodyWeightEntity)

    @Query("DELETE FROM body_weight WHERE id = :id")
    suspend fun deleteById(id: Long)
}
