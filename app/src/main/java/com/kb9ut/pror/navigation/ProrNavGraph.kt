package com.kb9ut.pror.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Calendar : Screen("calendar")
    data object Exercises : Screen("exercises")
    data object Progress : Screen("progress")
    data object Settings : Screen("settings")
    data object ActiveWorkout : Screen("active_workout/{workoutId}") {
        fun createRoute(workoutId: Long) = "active_workout/$workoutId"
    }
    data object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: Long) = "exercise_detail/$exerciseId"
    }
    data object WorkoutDetail : Screen("workout_detail/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_detail/$workoutId"
    }
    data object RoutineList : Screen("routine_list")
    data object RoutineEditor : Screen("routine_editor/{routineId}") {
        fun createRoute(routineId: Long) = "routine_editor/$routineId"
    }
    data object WorkoutSummary : Screen("workout_summary/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_summary/$workoutId"
    }
    data object PlateCalculator : Screen("plate_calculator")
    data object RmCalculator : Screen("rm_calculator")
}

data class BottomNavItem(
    val screen: Screen,
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
