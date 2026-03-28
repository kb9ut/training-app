package com.kb9ut.pror.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_set_templates",
    foreignKeys = [
        ForeignKey(
            entity = RoutineExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineExerciseId")]
)
data class RoutineSetTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineExerciseId: Long,
    val orderIndex: Int,
    val targetReps: Int? = null,
    val targetWeightKg: Double? = null,
    val targetRpe: Double? = null,
    val setType: SetType = SetType.NORMAL,
    val targetTempo: String? = null // Format: "E-P-C-T" e.g. "3-1-2-0"
)
