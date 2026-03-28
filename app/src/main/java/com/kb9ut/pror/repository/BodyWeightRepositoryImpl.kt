package com.kb9ut.pror.data.repository

import com.kb9ut.pror.data.local.dao.BodyWeightDao
import com.kb9ut.pror.data.local.entity.BodyWeightEntity
import com.kb9ut.pror.domain.repository.BodyWeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyWeightRepositoryImpl @Inject constructor(
    private val bodyWeightDao: BodyWeightDao
) : BodyWeightRepository {

    override fun getAllBodyWeights(): Flow<List<BodyWeightEntity>> =
        bodyWeightDao.getAllBodyWeights()

    override fun getBodyWeightsInRange(startTime: Long, endTime: Long): Flow<List<BodyWeightEntity>> =
        bodyWeightDao.getBodyWeightsInRange(startTime, endTime)

    override suspend fun getLatestBodyWeight(): BodyWeightEntity? =
        bodyWeightDao.getLatestBodyWeight()

    override suspend fun insertBodyWeight(bodyWeight: BodyWeightEntity): Long =
        bodyWeightDao.insert(bodyWeight)

    override suspend fun updateBodyWeight(bodyWeight: BodyWeightEntity) =
        bodyWeightDao.update(bodyWeight)

    override suspend fun deleteBodyWeight(id: Long) =
        bodyWeightDao.deleteById(id)
}
