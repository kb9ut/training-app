package com.kb9ut.pror.ui.screen.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.GoldPR
import com.kb9ut.pror.util.DateTimeUtils
import com.kb9ut.pror.util.FormatUtils
import com.kb9ut.pror.util.OneRepMaxCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    navController: NavController,
    viewModel: WorkoutDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            navController.popBackStack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_workout)) },
            text = { Text(stringResource(R.string.workout_detail_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteWorkout()
                }) {
                    Text(stringResource(R.string.done))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = { Text(uiState.workout?.name ?: "") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                }
            },
            actions = {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_workout)
                    )
                }
            }
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.workout == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_workouts_yet))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Summary card
                item {
                    SummaryCard(uiState = uiState)
                }

                // Exercise cards
                itemsIndexed(uiState.exercises) { _, exerciseDetail ->
                    ExerciseCard(exerciseDetail = exerciseDetail)
                }

                // Notes
                val notes = uiState.workout?.notes
                if (!notes.isNullOrBlank()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.notes).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrushedSteel,
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = notes,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(uiState: WorkoutDetailUiState) {
    val workout = uiState.workout ?: return

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date
            Text(
                text = DateTimeUtils.formatDateTime(workout.startedAt),
                style = MaterialTheme.typography.bodyMedium,
                color = BrushedSteel
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Duration
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.workout_detail_duration).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = workout.durationSeconds?.let { FormatUtils.formatDurationShort(it) } ?: "-",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Accent
                    )
                }

                // Total Volume
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.workout_detail_total_volume).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = FormatUtils.formatWeight(uiState.totalVolume, useLbs = false),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Accent
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(exerciseDetail: ExerciseDetailInfo) {
    val hasTempo = exerciseDetail.completedSets.any { it.tempo != null }
    val hasRir = exerciseDetail.completedSets.any { it.rir != null }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exerciseDetail.exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (exerciseDetail.completedSets.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.no_history_yet),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                // Table header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.set_number).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.weight).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.reps).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    if (hasRir) {
                        Text(
                            text = stringResource(R.string.rir).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = BrushedSteel,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (hasTempo) {
                        Text(
                            text = stringResource(R.string.tempo).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = BrushedSteel,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.tempo_tut).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = BrushedSteel,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = stringResource(R.string.workout_detail_estimated_1rm).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrushedSteel,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Set rows
                exerciseDetail.completedSets.forEachIndexed { index, set ->
                    val isBest = index == exerciseDetail.bestSetIndex
                    val estimated1RM = if (set.weightKg != null && set.reps != null && set.weightKg > 0 && set.reps > 0) {
                        OneRepMaxCalculator.calculateAll(set.weightKg, set.reps)?.median
                    } else null

                    val rowModifier = if (isBest) {
                        Modifier
                            .fillMaxWidth()
                            .background(
                                GoldPR.copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(vertical = 4.dp)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    }

                    Row(
                        modifier = rowModifier,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                            color = if (isBest) GoldPR else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = set.weightKg?.let { FormatUtils.formatWeight(it, useLbs = false) } ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = set.reps?.toString() ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        if (hasRir) {
                            Text(
                                text = set.rir?.toString() ?: "-",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = BrushedSteel
                            )
                        }
                        if (hasTempo) {
                            Text(
                                text = set.tempo ?: "-",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = BrushedSteel
                            )
                            Text(
                                text = FormatUtils.totalTut(set.tempo, set.reps)?.let { FormatUtils.formatTut(it) } ?: "-",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = Accent
                            )
                        }
                        Text(
                            text = estimated1RM?.let { FormatUtils.formatWeight(it, useLbs = false) } ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                            color = if (isBest) GoldPR else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
