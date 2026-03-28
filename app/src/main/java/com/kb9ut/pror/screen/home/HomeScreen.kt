package com.kb9ut.pror.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.components.AccentGlassCard
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.navigation.Screen
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.AccentDim
import com.kb9ut.pror.ui.theme.AccentSubtle
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.GlassBorderAccent
import com.kb9ut.pror.ui.theme.GoldPR
import com.kb9ut.pror.ui.theme.OnAccent
import com.kb9ut.pror.util.DateTimeUtils
import com.kb9ut.pror.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recentWorkouts by viewModel.recentWorkouts.collectAsStateWithLifecycle()
    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()
    val thisWeekCount by viewModel.thisWeekWorkoutCount.collectAsStateWithLifecycle()
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToWorkout.collect { workoutId ->
            navController.navigate(Screen.ActiveWorkout.createRoute(workoutId))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.app_name),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            },
            actions = {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            windowInsets = WindowInsets(0),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Routines quick-select
            item(key = "routines_section") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.routines).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp
                    )
                    if (routines.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.routine_new),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = Accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { navController.navigate(Screen.RoutineList.route) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (routines.isEmpty()) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { navController.navigate(Screen.RoutineList.route) }
                    ) {
                        Text(
                            text = stringResource(R.string.home_no_routines),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = routines,
                            key = { it.id }
                        ) { routine ->
                            RoutineChipCard(
                                routineName = routine.name,
                                onClick = { viewModel.startWorkoutFromRoutine(routine.id) }
                            )
                        }
                    }
                }
            }

            // Row 2: Stats Summary (2 columns)
            item(key = "stats_row") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        label = stringResource(R.string.home_this_week),
                        value = stringResource(R.string.home_workouts_count, thisWeekCount),
                        icon = {
                            Icon(
                                Icons.Filled.Timeline,
                                contentDescription = stringResource(R.string.cd_this_week_stats),
                                modifier = Modifier.size(20.dp),
                                tint = Accent
                            )
                        },
                        valueColor = Accent,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = stringResource(R.string.home_streak),
                        value = stringResource(R.string.home_days_count, streak),
                        icon = {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = stringResource(R.string.cd_streak),
                                modifier = Modifier.size(20.dp),
                                tint = GoldPR
                            )
                        },
                        valueColor = GoldPR,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 3: Quick Action (full width)
            item(key = "quick_action") {
                if (activeWorkout != null) {
                    ContinueWorkoutCard(
                        elapsedSeconds = ((System.currentTimeMillis() - activeWorkout!!.startedAt) / 1000).toInt(),
                        onClick = {
                            navController.navigate(
                                Screen.ActiveWorkout.createRoute(activeWorkout!!.id)
                            )
                        }
                    )
                } else {
                    QuickStartCard(
                        onClick = { viewModel.startEmptyWorkout() }
                    )
                }
            }

            // Row 4: Recent Workouts (full width)
            if (recentWorkouts.isNotEmpty()) {
                item(key = "recent_header") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.recent_workouts).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp
                    )
                }

                item(key = "recent_workouts_card") {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            recentWorkouts.forEachIndexed { index, workout ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(
                                            when (index) {
                                                0 -> RoundedCornerShape(
                                                    topStart = 24.dp,
                                                    topEnd = 24.dp
                                                )

                                                recentWorkouts.lastIndex -> RoundedCornerShape(
                                                    bottomStart = 24.dp,
                                                    bottomEnd = 24.dp
                                                )

                                                else -> RoundedCornerShape(0.dp)
                                            }
                                        )
                                        .clickable {
                                            navController.navigate(
                                                Screen.WorkoutDetail.createRoute(workout.id)
                                            )
                                        }
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = workout.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = DateTimeUtils.formatDate(workout.startedAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    workout.durationSeconds?.let { seconds ->
                                        Text(
                                            text = FormatUtils.formatDurationShort(seconds),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Accent
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = BrushedSteel
                                    )
                                }
                                if (index < recentWorkouts.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (recentWorkouts.isEmpty()) {
                item(key = "empty_state") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.no_workouts_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Row 4: Quick Links (2 columns)
            item(key = "quick_links") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickLinkCard(
                        title = stringResource(R.string.routines),
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = stringResource(R.string.cd_routines_link),
                                modifier = Modifier.size(24.dp),
                                tint = Accent
                            )
                        },
                        onClick = { navController.navigate(Screen.RoutineList.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickLinkCard(
                        title = stringResource(R.string.nav_exercises),
                        icon = {
                            Icon(
                                Icons.Filled.FitnessCenter,
                                contentDescription = stringResource(R.string.cd_exercises_link),
                                modifier = Modifier.size(24.dp),
                                tint = Accent
                            )
                        },
                        onClick = {
                            navController.navigate(Screen.Exercises.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 5: Calculator quick links (2 columns)
            item(key = "calculator_links") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickLinkCard(
                        title = stringResource(R.string.plate_calculator),
                        icon = {
                            Icon(
                                Icons.Filled.Calculate,
                                contentDescription = stringResource(R.string.cd_plate_calculator),
                                modifier = Modifier.size(24.dp),
                                tint = Accent
                            )
                        },
                        onClick = { navController.navigate(Screen.PlateCalculator.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickLinkCard(
                        title = stringResource(R.string.rm_calculator),
                        icon = {
                            Icon(
                                Icons.Filled.Functions,
                                contentDescription = stringResource(R.string.cd_rm_calculator),
                                modifier = Modifier.size(24.dp),
                                tint = Accent
                            )
                        },
                        onClick = { navController.navigate(Screen.RmCalculator.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStartCard(
    onClick: () -> Unit
) {
    AccentGlassCard(
        modifier = Modifier
            .fillMaxWidth(),
        cornerRadius = 24.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = stringResource(R.string.cd_quick_start),
                tint = Accent,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.home_quick_start),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Accent,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun ContinueWorkoutCard(
    elapsedSeconds: Int,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        borderColor = GlassBorderAccent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.workout_in_progress),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = AccentDim
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.continue_workout),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Accent
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = FormatUtils.formatDuration(elapsedSeconds),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Accent,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        gradient = true
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun RoutineChipCard(
    routineName: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = stringResource(R.string.cd_routine_card, routineName),
                tint = Accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = routineName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickLinkCard(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
