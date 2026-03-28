package com.kb9ut.pror.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.AccentSubtle
import com.kb9ut.pror.ui.theme.DarkSteel
import com.kb9ut.pror.ui.theme.OnSurfaceVariantDark
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.navigation.BottomNavItem
import com.kb9ut.pror.ui.navigation.Screen
import com.kb9ut.pror.ui.screen.activeworkout.ActiveWorkoutScreen
import com.kb9ut.pror.ui.screen.calendar.CalendarScreen
import com.kb9ut.pror.ui.screen.exercises.ExerciseDetailScreen
import com.kb9ut.pror.ui.screen.exercises.ExerciseListScreen
import com.kb9ut.pror.ui.screen.home.HomeScreen
import com.kb9ut.pror.ui.screen.progress.ProgressScreen
import com.kb9ut.pror.ui.screen.routine.RoutineEditorScreen
import com.kb9ut.pror.ui.screen.routine.RoutineListScreen
import com.kb9ut.pror.ui.screen.settings.SettingsScreen
import com.kb9ut.pror.ui.screen.workout.WorkoutDetailScreen
import com.kb9ut.pror.ui.screen.platecalculator.PlateCalculatorScreen
import com.kb9ut.pror.ui.screen.rmcalculator.RmCalculatorScreen
import com.kb9ut.pror.ui.screen.workout.WorkoutSummaryScreen
import com.kb9ut.pror.ui.theme.ProrTheme

@Composable
fun ProrApp() {
    ProrTheme {
        val navController = rememberNavController()

        val bottomNavItems = listOf(
            BottomNavItem(Screen.Home, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavItem(Screen.Calendar, R.string.nav_calendar, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
            BottomNavItem(Screen.Exercises, R.string.nav_exercises, Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
            BottomNavItem(Screen.Progress, R.string.nav_progress, Icons.AutoMirrored.Filled.ShowChart, Icons.AutoMirrored.Outlined.ShowChart)
        )

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val showBottomBar = bottomNavItems.any { item ->
            currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    Column {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = DarkSteel
                        )
                        NavigationBar(
                            containerColor = Color.Black,
                            tonalElevation = 0.dp
                        ) {
                            bottomNavItems.forEach { item ->
                                val selected = currentDestination?.hierarchy?.any {
                                    it.route == item.screen.route
                                } == true

                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = stringResource(item.labelResId)
                                        )
                                    },
                                    label = { Text(stringResource(item.labelResId)) },
                                    selected = selected,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Accent,
                                        selectedTextColor = Accent,
                                        unselectedIconColor = OnSurfaceVariantDark,
                                        unselectedTextColor = OnSurfaceVariantDark,
                                        indicatorColor = AccentSubtle
                                    ),
                                    onClick = {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(navController = navController)
                }
                composable(Screen.Calendar.route) {
                    CalendarScreen(navController = navController)
                }
                composable(Screen.Exercises.route) {
                    ExerciseListScreen(navController = navController)
                }
                composable(Screen.Progress.route) {
                    ProgressScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(navController = navController)
                }
                composable(
                    route = Screen.ExerciseDetail.route,
                    arguments = listOf(
                        navArgument("exerciseId") { type = NavType.LongType }
                    )
                ) {
                    ExerciseDetailScreen(navController = navController)
                }
                composable(
                    route = Screen.ActiveWorkout.route,
                    arguments = listOf(
                        navArgument("workoutId") { type = NavType.LongType }
                    )
                ) {
                    ActiveWorkoutScreen(navController = navController)
                }
                composable(
                    route = Screen.WorkoutDetail.route,
                    arguments = listOf(
                        navArgument("workoutId") { type = NavType.LongType }
                    )
                ) {
                    WorkoutDetailScreen(navController = navController)
                }
                composable(Screen.RoutineList.route) {
                    RoutineListScreen(navController = navController)
                }
                composable(
                    route = Screen.RoutineEditor.route,
                    arguments = listOf(
                        navArgument("routineId") { type = NavType.LongType }
                    )
                ) {
                    RoutineEditorScreen(navController = navController)
                }
                composable(
                    route = Screen.WorkoutSummary.route,
                    arguments = listOf(
                        navArgument("workoutId") { type = NavType.LongType }
                    )
                ) {
                    WorkoutSummaryScreen(navController = navController)
                }
                composable(Screen.PlateCalculator.route) {
                    PlateCalculatorScreen(navController = navController)
                }
                composable(Screen.RmCalculator.route) {
                    RmCalculatorScreen(navController = navController)
                }
            }
        }
    }
}
