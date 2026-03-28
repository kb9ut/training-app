package com.kb9ut.pror.data.local.seed

import com.kb9ut.pror.R

data class ProgramPreset(
    val nameResId: Int,
    val descriptionResId: Int,
    val routines: List<RoutinePreset>
)

data class RoutinePreset(
    val nameResId: Int,
    val exercises: List<ExercisePreset>
)

data class ExercisePreset(
    val exerciseName: String,
    val sets: Int,
    val targetReps: Int
)

object ProgramPresets {

    fun getAll(): List<ProgramPreset> = listOf(
        pushPullLegs(),
        upperLowerSplit(),
        fullBody()
    )

    private fun pushPullLegs() = ProgramPreset(
        nameResId = R.string.program_ppl,
        descriptionResId = R.string.program_ppl_desc,
        routines = listOf(
            RoutinePreset(
                nameResId = R.string.program_push_day,
                exercises = listOf(
                    ExercisePreset("Barbell Bench Press", sets = 4, targetReps = 8),
                    ExercisePreset("Overhead Press", sets = 3, targetReps = 8),
                    ExercisePreset("Incline Dumbbell Bench Press", sets = 3, targetReps = 10),
                    ExercisePreset("Lateral Raise", sets = 4, targetReps = 12),
                    ExercisePreset("Tricep Pushdown", sets = 3, targetReps = 12),
                    ExercisePreset("Overhead Tricep Extension", sets = 3, targetReps = 12)
                )
            ),
            RoutinePreset(
                nameResId = R.string.program_pull_day,
                exercises = listOf(
                    ExercisePreset("Barbell Row", sets = 4, targetReps = 8),
                    ExercisePreset("Pull Up", sets = 3, targetReps = 8),
                    ExercisePreset("Face Pull", sets = 4, targetReps = 15),
                    ExercisePreset("Barbell Curl", sets = 3, targetReps = 10),
                    ExercisePreset("Hammer Curl", sets = 3, targetReps = 12)
                )
            ),
            RoutinePreset(
                nameResId = R.string.program_leg_day,
                exercises = listOf(
                    ExercisePreset("Barbell Squat", sets = 4, targetReps = 8),
                    ExercisePreset("Romanian Deadlift", sets = 3, targetReps = 10),
                    ExercisePreset("Leg Press", sets = 3, targetReps = 12),
                    ExercisePreset("Lying Leg Curl", sets = 3, targetReps = 12),
                    ExercisePreset("Standing Calf Raise", sets = 4, targetReps = 15)
                )
            )
        )
    )

    private fun upperLowerSplit() = ProgramPreset(
        nameResId = R.string.program_upper_lower,
        descriptionResId = R.string.program_upper_lower_desc,
        routines = listOf(
            RoutinePreset(
                nameResId = R.string.program_upper_day,
                exercises = listOf(
                    ExercisePreset("Barbell Bench Press", sets = 4, targetReps = 8),
                    ExercisePreset("Barbell Row", sets = 4, targetReps = 8),
                    ExercisePreset("Overhead Press", sets = 3, targetReps = 10),
                    ExercisePreset("Pull Up", sets = 3, targetReps = 8),
                    ExercisePreset("Lateral Raise", sets = 3, targetReps = 12),
                    ExercisePreset("Barbell Curl", sets = 3, targetReps = 10),
                    ExercisePreset("Tricep Pushdown", sets = 3, targetReps = 12)
                )
            ),
            RoutinePreset(
                nameResId = R.string.program_lower_day,
                exercises = listOf(
                    ExercisePreset("Barbell Squat", sets = 4, targetReps = 8),
                    ExercisePreset("Romanian Deadlift", sets = 3, targetReps = 10),
                    ExercisePreset("Leg Press", sets = 3, targetReps = 12),
                    ExercisePreset("Lying Leg Curl", sets = 3, targetReps = 12),
                    ExercisePreset("Standing Calf Raise", sets = 4, targetReps = 15)
                )
            )
        )
    )

    private fun fullBody() = ProgramPreset(
        nameResId = R.string.program_full_body,
        descriptionResId = R.string.program_full_body_desc,
        routines = listOf(
            RoutinePreset(
                nameResId = R.string.program_full_body,
                exercises = listOf(
                    ExercisePreset("Barbell Squat", sets = 4, targetReps = 8),
                    ExercisePreset("Barbell Bench Press", sets = 4, targetReps = 8),
                    ExercisePreset("Barbell Row", sets = 4, targetReps = 8),
                    ExercisePreset("Overhead Press", sets = 3, targetReps = 10),
                    ExercisePreset("Romanian Deadlift", sets = 3, targetReps = 10),
                    ExercisePreset("Barbell Curl", sets = 3, targetReps = 10)
                )
            )
        )
    )
}
