package com.kb9ut.pror.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class ReppenBackup(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val exercises: List<ExerciseBackup>,
    val workouts: List<WorkoutBackup>,
    val routines: List<RoutineBackup>,
    val bodyWeights: List<BodyWeightBackup>
)

@Serializable
data class ExerciseBackup(
    val id: Long,
    val name: String,
    val muscleGroup: String,
    val secondaryMuscleGroup: String?,
    val equipmentCategory: String,
    val isCustom: Boolean,
    val notes: String?
)

@Serializable
data class WorkoutBackup(
    val id: Long,
    val name: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val durationSeconds: Int?,
    val notes: String?,
    val exercises: List<WorkoutExerciseBackup>
)

@Serializable
data class WorkoutExerciseBackup(
    val exerciseId: Long,
    val orderIndex: Int,
    val notes: String?,
    val sets: List<WorkoutSetBackup>
)

@Serializable
data class WorkoutSetBackup(
    val orderIndex: Int,
    val weightKg: Double?,
    val reps: Int?,
    val rpe: Double?,
    val rir: Int?,
    val setType: String,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val tempo: String? = null
)

@Serializable
data class RoutineBackup(
    val id: Long,
    val name: String,
    val description: String?,
    val exercises: List<RoutineExerciseBackup>
)

@Serializable
data class RoutineExerciseBackup(
    val exerciseId: Long,
    val orderIndex: Int,
    val sets: List<RoutineSetTemplateBackup>,
    val notes: String? = null
)

@Serializable
data class RoutineSetTemplateBackup(
    val orderIndex: Int,
    val targetReps: Int?,
    val targetWeightKg: Double?,
    val targetRpe: Double?,
    val setType: String,
    val targetTempo: String? = null
)

@Serializable
data class BodyWeightBackup(
    val weightKg: Double,
    val recordedAt: Long,
    val notes: String?
)
