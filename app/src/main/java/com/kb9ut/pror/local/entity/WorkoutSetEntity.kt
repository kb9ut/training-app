package com.kb9ut.pror.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutExerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutExerciseId: Long,
    val orderIndex: Int,
    val weightKg: Double? = null,
    val reps: Int? = null,
    val rpe: Double? = null,
    val rir: Int? = null,
    val setType: SetType = SetType.NORMAL,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val tempo: String? = null // Format: "E-P-C-T" e.g. "3-1-2-0"
)
