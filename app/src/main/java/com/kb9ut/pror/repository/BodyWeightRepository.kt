package com.kb9ut.pror.domain.repository

import com.kb9ut.pror.data.local.entity.BodyWeightEntity
import kotlinx.coroutines.flow.Flow

interface BodyWeightRepository {
    fun getAllBodyWeights(): Flow<List<BodyWeightEntity>>
    fun getBodyWeightsInRange(startTime: Long, endTime: Long): Flow<List<BodyWeightEntity>>
    suspend fun getLatestBodyWeight(): BodyWeightEntity?
    suspend fun insertBodyWeight(bodyWeight: BodyWeightEntity): Long
    suspend fun updateBodyWeight(bodyWeight: BodyWeightEntity)
    suspend fun deleteBodyWeight(id: Long)
}
