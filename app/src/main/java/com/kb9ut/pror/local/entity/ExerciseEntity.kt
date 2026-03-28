package com.kb9ut.pror.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: MuscleGroup,
    val secondaryMuscleGroup: MuscleGroup? = null,
    val equipmentCategory: EquipmentCategory,
    val isCustom: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
