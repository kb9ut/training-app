package com.kb9ut.pror.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_weight")
data class BodyWeightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Double,
    val recordedAt: Long = System.currentTimeMillis(),
    val notes: String? = null
)
