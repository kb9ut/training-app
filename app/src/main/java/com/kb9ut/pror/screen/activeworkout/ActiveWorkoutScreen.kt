package com.kb9ut.pror.ui.screen.activeworkout

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kb9ut.pror.R
import com.kb9ut.pror.data.local.entity.GroupType
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity
import com.kb9ut.pror.service.MetronomeState
import com.kb9ut.pror.service.TempoPhase
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.components.PrAchievementOverlay
import com.kb9ut.pror.ui.components.RestTimerOverlay
import com.kb9ut.pror.ui.components.TempoDialog
import com.kb9ut.pror.ui.navigation.Screen
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.AccentSubtle
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.GlassBackgroundElevated
import com.kb9ut.pror.ui.theme.GlassBorder
import com.kb9ut.pror.ui.theme.NeonRed
import com.kb9ut.pror.ui.theme.Tertiary
import com.kb9ut.pror.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    navController: NavController,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val workoutName by viewModel.workoutName.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val workoutFinished by viewModel.workoutFinished.collectAsStateWithLifecycle()
    val workoutDiscarded by viewModel.workoutDiscarded.collectAsStateWithLifecycle()
    val metronomeState by viewModel.metronomeState.collectAsStateWithLifecycle()
    val metronomeActiveExerciseId by viewModel.metronomeActiveExerciseId.collectAsStateWithLifecycle()

    var showExercisePicker by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var currentPrAchievement by remember { mutableStateOf<PrAchievement?>(null) }

    LaunchedEffect(Unit) {
        viewModel.prAchievementEvent.collect { pr ->
            currentPrAchievement = pr
        }
    }

    LaunchedEffect(workoutFinished) {
        if (workoutFinished) {
            navController.navigate(Screen.WorkoutSummary.createRoute(viewModel.workoutId)) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
        }
    }

    LaunchedEffect(workoutDiscarded) {
        if (workoutDiscarded) {
            navController.popBackStack(Screen.Home.route, inclusive = false)
        }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text(stringResource(R.string.finish_workout)) },
            text = { Text(stringResource(R.string.finish_workout_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    viewModel.finishWorkout()
                }) {
                    Text(stringResource(R.string.done))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_workout)) },
            text = { Text(stringResource(R.string.discard_workout_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.discardWorkout()
                }) {
                    Text(stringResource(R.string.discard_workout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            navigationIcon = {
                IconButton(onClick = { showDiscardDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close)
                    )
                }
            },
            title = {
                if (isEditingName) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        textStyle = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Column(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            editedName = workoutName
                            isEditingName = true
                        }
                    ) {
                        Text(
                            text = workoutName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        ElapsedTimerText(viewModel = viewModel)
                    }
                }
            },
            actions = {
                if (isEditingName) {
                    TextButton(onClick = {
                        viewModel.updateWorkoutName(editedName)
                        isEditingName = false
                    }) {
                        Text(stringResource(R.string.save))
                    }
                } else {
                    Button(
                        onClick = { showFinishDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.finish_workout),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )

        if (exercises.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.no_exercises_added),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.tap_to_add_exercise),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val groupedItems by remember(exercises) {
                derivedStateOf { buildExerciseListItems(exercises) }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                items(groupedItems.size, key = { groupedItems[it].key }) { idx ->
                    when (val item = groupedItems[idx]) {
                        is ExerciseListItem.GroupHeader -> {
                            val label = when (item.groupType) {
                                GroupType.SUPERSET -> stringResource(R.string.superset)
                                GroupType.GIANT_SET -> stringResource(R.string.triset)
                                GroupType.CIRCUIT -> stringResource(R.string.circuit)
                                else -> ""
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonRed,
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 12.dp, bottom = 4.dp)
                                    .background(
                                        color = NeonRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        is ExerciseListItem.Exercise -> {
                            val exerciseState = item.state
                            val isGrouped = exerciseState.groupId != null

                            if (isGrouped) {
                                // Grouped exercise with colored bar on left
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                        .padding(bottom = if (item.isLastInGroup) 0.dp else 2.dp)
                                ) {
                                    // Vertical colored bar
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight()
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = if (item.isFirstInGroup) 4.dp else 0.dp,
                                                    topEnd = if (item.isFirstInGroup) 4.dp else 0.dp,
                                                    bottomStart = if (item.isLastInGroup) 4.dp else 0.dp,
                                                    bottomEnd = if (item.isLastInGroup) 4.dp else 0.dp
                                                )
                                            )
                                            .background(Accent)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ExerciseCard(
                                        exerciseState = exerciseState,
                                        onSetCompleted = { setId -> viewModel.completeSet(setId) },
                                        onSetUpdated = { set -> viewModel.updateSet(set) },
                                        onSetDeleted = { setId -> viewModel.deleteSet(setId) },
                                        onAddSet = { viewModel.addSet(exerciseState.workoutExerciseId) },
                                        onRemoveExercise = { viewModel.removeExercise(exerciseState.workoutExerciseId) },
                                        onRestTimerChanged = { seconds ->
                                            viewModel.setExerciseRestTimer(exerciseState.workoutExerciseId, seconds)
                                        },
                                        onNotesChanged = { notes ->
                                            viewModel.updateExerciseNotes(exerciseState.workoutExerciseId, notes)
                                        },
                                        onMetronomeToggle = { weId, tempo -> viewModel.toggleMetronome(weId, tempo) },
                                        metronomeActiveExerciseId = metronomeActiveExerciseId,
                                        metronomeState = metronomeState,
                                        isGrouped = true,
                                        onRemoveFromGroup = { viewModel.removeFromSuperset(exerciseState.workoutExerciseId) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                ExerciseCard(
                                    exerciseState = exerciseState,
                                    onSetCompleted = { setId -> viewModel.completeSet(setId) },
                                    onSetUpdated = { set -> viewModel.updateSet(set) },
                                    onSetDeleted = { setId -> viewModel.deleteSet(setId) },
                                    onAddSet = { viewModel.addSet(exerciseState.workoutExerciseId) },
                                    onRemoveExercise = { viewModel.removeExercise(exerciseState.workoutExerciseId) },
                                    onRestTimerChanged = { seconds ->
                                        viewModel.setExerciseRestTimer(exerciseState.workoutExerciseId, seconds)
                                    },
                                    onNotesChanged = { notes ->
                                        viewModel.updateExerciseNotes(exerciseState.workoutExerciseId, notes)
                                    },
                                    onMetronomeToggle = { weId, tempo -> viewModel.toggleMetronome(weId, tempo) },
                                    metronomeActiveExerciseId = metronomeActiveExerciseId,
                                    metronomeState = metronomeState,
                                    isGrouped = false,
                                    onRemoveFromGroup = {},
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        is ExerciseListItem.LinkButton -> {
                            val isCurrentlyLinked = item.isLinked
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(
                                    onClick = {
                                        if (isCurrentlyLinked) {
                                            // Unlink: remove the second exercise from the group
                                            viewModel.removeFromSuperset(item.exerciseBelowId)
                                        } else {
                                            viewModel.createSuperset(item.exerciseAboveId, item.exerciseBelowId)
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCurrentlyLinked) Icons.Filled.LinkOff else Icons.Filled.Link,
                                        contentDescription = if (isCurrentlyLinked) {
                                            stringResource(R.string.remove_from_superset)
                                        } else {
                                            stringResource(R.string.link_exercises)
                                        },
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isCurrentlyLinked) Tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isCurrentlyLinked) {
                                            stringResource(R.string.remove_from_superset)
                                        } else {
                                            stringResource(R.string.link_exercises)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCurrentlyLinked) Tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rest Timer Overlay - reads its own StateFlow to avoid recomposing the entire screen
        IsolatedRestTimerOverlay(viewModel = viewModel)

        // Footer: Add Exercise button
        Button(
            onClick = { showExercisePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 12.dp, vertical = 0.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Accent
            ),
            border = BorderStroke(1.dp, Accent)
        ) {
            Text(
                text = stringResource(R.string.add_exercise_to_workout),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Accent
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

        // PR Achievement Overlay
        PrAchievementOverlay(
            achievement = currentPrAchievement,
            onDismiss = { currentPrAchievement = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    if (showExercisePicker) {
        ExercisePickerSheet(
            exerciseRepository = viewModel.exerciseRepository,
            onExerciseSelected = { exerciseId ->
                viewModel.addExercise(exerciseId)
            },
            onDismiss = { showExercisePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseCard(
    exerciseState: WorkoutExerciseUiState,
    onSetCompleted: (Long) -> Unit,
    onSetUpdated: (WorkoutSetEntity) -> Unit,
    onSetDeleted: (Long) -> Unit,
    onAddSet: () -> Unit,
    onRemoveExercise: () -> Unit,
    onRestTimerChanged: (Int) -> Unit,
    onNotesChanged: (String) -> Unit,
    onMetronomeToggle: (Long, String?) -> Unit,
    metronomeActiveExerciseId: Long?,
    metronomeState: MetronomeState,
    isGrouped: Boolean = false,
    onRemoveFromGroup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        elevated = true
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            var isNotesExpanded by remember { mutableStateOf(false) }
            var notesText by remember(exerciseState.workoutExerciseId, exerciseState.notes) {
                mutableStateOf(exerciseState.notes ?: "")
            }

            // Determine exercise-level metronome tempo from first set that has tempo
            val exerciseTempo = exerciseState.sets.firstOrNull { it.tempo != null }?.tempo
            val isMetronomeActive = metronomeActiveExerciseId == exerciseState.workoutExerciseId

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseState.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                // Metronome toggle (only show if any set has tempo)
                if (exerciseTempo != null) {
                    val metronomeToggleDesc = stringResource(R.string.cd_metronome_toggle)
                    IconButton(
                        onClick = { onMetronomeToggle(exerciseState.workoutExerciseId, exerciseTempo) },
                        modifier = Modifier
                            .size(36.dp)
                            .semantics { contentDescription = metronomeToggleDesc }
                    ) {
                        Icon(
                            imageVector = if (isMetronomeActive) Icons.Filled.MusicNote else Icons.Filled.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isMetronomeActive) Accent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = { isNotesExpanded = !isNotesExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.StickyNote2,
                        contentDescription = stringResource(R.string.cd_toggle_notes),
                        modifier = Modifier.size(20.dp),
                        tint = if (isNotesExpanded || !exerciseState.notes.isNullOrBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                IconButton(
                    onClick = onRemoveExercise,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.remove_exercise),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Metronome phase/countdown display (below header, above sets)
            if (isMetronomeActive && metronomeState.isPlaying) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            color = Accent.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Accent
                    )
                    val phaseText = when (metronomeState.currentPhase) {
                        TempoPhase.ECCENTRIC -> stringResource(R.string.tempo_phase_eccentric)
                        TempoPhase.PAUSE_BOTTOM -> stringResource(R.string.tempo_phase_pause)
                        TempoPhase.CONCENTRIC -> stringResource(R.string.tempo_phase_concentric)
                        TempoPhase.PAUSE_TOP -> stringResource(R.string.tempo_phase_pause)
                    }
                    Text(
                        text = "$phaseText ${metronomeState.phaseSecondsRemaining}s",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Accent
                    )
                    Text(
                        text = "#${metronomeState.currentRep}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Per-workout-exercise notes (editable)
            if (isNotesExpanded) {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { newValue ->
                        notesText = newValue
                        onNotesChanged(newValue)
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.workout_exercise_notes_hint),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = GlassBorder,
                        focusedBorderColor = Accent,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E)
                    ),
                    minLines = 1,
                    maxLines = 3
                )
            } else if (!exerciseState.notes.isNullOrBlank()) {
                Text(
                    text = exerciseState.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable { isNotesExpanded = true }
                )
            }

            // Exercise notes
            if (!exerciseState.exercise.notes.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.StickyNote2,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = exerciseState.exercise.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Per-exercise rest timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.rest_timer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val restOptions = listOf(30, 60, 90, 120, 180, 300)
                restOptions.forEach { seconds ->
                    val isSelected = exerciseState.restTimerSeconds == seconds
                    val label = if (seconds < 60) "${seconds}s" else "${seconds / 60}m${if (seconds % 60 > 0) "${seconds % 60}s" else ""}"
                    TextButton(
                        onClick = { onRestTimerChanged(seconds) },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Column headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrushedSteel,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.previous),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrushedSteel,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.kg),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrushedSteel,
                    modifier = Modifier.width(72.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.reps),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrushedSteel,
                    modifier = Modifier.width(56.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sets
            exerciseState.sets.forEachIndexed { index, set ->
                val previousSet = exerciseState.previousSets.getOrNull(index)
                SetRow(
                    setNumber = index + 1,
                    set = set,
                    previousSet = previousSet,
                    onSetCompleted = { onSetCompleted(set.id) },
                    onSetUpdated = onSetUpdated,
                    onSetDeleted = { onSetDeleted(set.id) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ ${stringResource(R.string.add_set)}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetRow(
    setNumber: Int,
    set: WorkoutSetEntity,
    previousSet: WorkoutSetEntity?,
    onSetCompleted: () -> Unit,
    onSetUpdated: (WorkoutSetEntity) -> Unit,
    onSetDeleted: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSetDeleted()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        var weightText by remember(set.id, set.weightKg) {
            mutableStateOf(set.weightKg?.let { formatWeightInput(it) } ?: "")
        }
        var repsText by remember(set.id, set.reps) {
            mutableStateOf(set.reps?.toString() ?: "")
        }
        var showTempoDialog by remember { mutableStateOf(false) }

        if (showTempoDialog) {
            TempoDialog(
                currentTempo = set.tempo,
                onTempoSet = { tempo ->
                    onSetUpdated(set.copy(tempo = tempo))
                    showTempoDialog = false
                },
                onDismiss = { showTempoDialog = false }
            )
        }

        val rowBackground by animateColorAsState(
            targetValue = if (set.isCompleted) {
                Accent.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            label = "rowBackground"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBackground)
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = setNumber.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )

                // Previous value
                val previousText = if (previousSet != null && previousSet.weightKg != null && previousSet.reps != null) {
                    "${formatWeightInput(previousSet.weightKg)}kg \u00D7 ${previousSet.reps}"
                } else {
                    "\u2014"
                }
                val isPreviousValid = previousSet != null && previousSet.weightKg != null && previousSet.reps != null
                val copyPreviousDesc = stringResource(R.string.cd_copy_previous_set)
                Text(
                    text = previousText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPreviousValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (isPreviousValid) {
                                Modifier
                                    .semantics { contentDescription = copyPreviousDesc }
                                    .clickable {
                                        weightText = formatWeightInput(previousSet.weightKg)
                                        repsText = previousSet.reps.toString()
                                        onSetUpdated(
                                            set.copy(
                                                weightKg = previousSet.weightKg,
                                                reps = previousSet.reps
                                            )
                                        )
                                    }
                            } else {
                                Modifier
                            }
                        ),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { newValue ->
                        weightText = newValue
                        val weight = newValue.toDoubleOrNull()
                        onSetUpdated(set.copy(weightKg = weight))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.width(72.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Accent,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E)
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = repsText,
                    onValueChange = { newValue ->
                        repsText = newValue
                        val reps = newValue.toIntOrNull()
                        onSetUpdated(set.copy(reps = reps))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(56.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Accent,
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E)
                    )
                )

                val checkColor by animateColorAsState(
                    targetValue = if (set.isCompleted) {
                        Accent
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "checkColor"
                )

                IconButton(
                    onClick = onSetCompleted,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = stringResource(R.string.cd_complete_set),
                        tint = checkColor
                    )
                }
            }

            // Tempo & RIR display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tempoLabel = set.tempo ?: stringResource(R.string.tempo)
                val tutText = FormatUtils.totalTut(set.tempo, set.reps)?.let { " (${FormatUtils.formatTut(it)})" } ?: ""
                val editTempoDesc = stringResource(R.string.cd_edit_tempo)
                Text(
                    text = tempoLabel + tutText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (set.tempo != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .semantics { contentDescription = editTempoDesc }
                        .clickable { showTempoDialog = true }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // RIR selector
                val rirLabel = stringResource(R.string.rir)
                val rirValues = listOf<Int?>(null, 0, 1, 2, 3, 4, 5)
                val currentRirIndex = rirValues.indexOf(set.rir)
                Text(
                    text = if (set.rir != null) "$rirLabel ${set.rir}" else rirLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (set.rir != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.clickable {
                        val nextIndex = (currentRirIndex + 1) % rirValues.size
                        val newRir = rirValues[nextIndex]
                        onSetUpdated(set.copy(rir = newRir))
                    }
                )
            }
        }
    }
}

/**
 * Sealed class representing items in the exercise list.
 * Used to build a flat list that includes group headers, exercise cards, and link buttons.
 */
private sealed class ExerciseListItem(val key: String) {
    data class GroupHeader(val groupId: Long, val groupType: GroupType) : ExerciseListItem("header_$groupId")
    data class Exercise(
        val state: WorkoutExerciseUiState,
        val isFirstInGroup: Boolean = false,
        val isLastInGroup: Boolean = false
    ) : ExerciseListItem("exercise_${state.workoutExerciseId}")
    data class LinkButton(
        val exerciseAboveId: Long,
        val exerciseBelowId: Long,
        val isLinked: Boolean
    ) : ExerciseListItem("link_${exerciseAboveId}_$exerciseBelowId")
}

/**
 * Build a flat list of ExerciseListItems from the exercises.
 * Groups consecutive exercises that share the same groupId.
 * Between each pair of exercises, inserts a LinkButton.
 */
private fun buildExerciseListItems(exercises: List<WorkoutExerciseUiState>): List<ExerciseListItem> {
    if (exercises.isEmpty()) return emptyList()

    val result = mutableListOf<ExerciseListItem>()
    var i = 0

    while (i < exercises.size) {
        val current = exercises[i]
        val groupId = current.groupId

        if (groupId != null) {
            // Collect all consecutive exercises in the same group
            val groupMembers = mutableListOf(current)
            var j = i + 1
            while (j < exercises.size && exercises[j].groupId == groupId) {
                groupMembers.add(exercises[j])
                j++
            }

            // Add group header
            result.add(ExerciseListItem.GroupHeader(groupId, current.groupType))

            // Add exercises in the group
            groupMembers.forEachIndexed { idx, member ->
                result.add(
                    ExerciseListItem.Exercise(
                        state = member,
                        isFirstInGroup = idx == 0,
                        isLastInGroup = idx == groupMembers.size - 1
                    )
                )
                // Add link button between group members
                if (idx < groupMembers.size - 1) {
                    val next = groupMembers[idx + 1]
                    result.add(
                        ExerciseListItem.LinkButton(
                            exerciseAboveId = member.workoutExerciseId,
                            exerciseBelowId = next.workoutExerciseId,
                            isLinked = true
                        )
                    )
                }
            }

            // Add link button to next exercise (if any and not in same group)
            if (j < exercises.size) {
                result.add(
                    ExerciseListItem.LinkButton(
                        exerciseAboveId = groupMembers.last().workoutExerciseId,
                        exerciseBelowId = exercises[j].workoutExerciseId,
                        isLinked = false
                    )
                )
            }

            i = j
        } else {
            // Ungrouped exercise
            result.add(ExerciseListItem.Exercise(state = current))

            // Add link button to next exercise (if any)
            if (i + 1 < exercises.size) {
                val next = exercises[i + 1]
                val isLinked = next.groupId != null && current.groupId == next.groupId
                result.add(
                    ExerciseListItem.LinkButton(
                        exerciseAboveId = current.workoutExerciseId,
                        exerciseBelowId = next.workoutExerciseId,
                        isLinked = isLinked
                    )
                )
            }

            i++
        }
    }

    return result
}

private fun formatWeightInput(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) {
        weight.toLong().toString()
    } else {
        weight.toString()
    }
}

/**
 * Isolated composable that collects elapsed seconds from the ViewModel.
 * By reading the StateFlow here instead of at the top-level ActiveWorkoutScreen,
 * only this small Text widget recomposes every second -- not the entire screen.
 */
@Composable
private fun ElapsedTimerText(viewModel: ActiveWorkoutViewModel) {
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    Text(
        text = FormatUtils.formatDuration(elapsedSeconds),
        style = MaterialTheme.typography.labelMedium,
        color = Accent
    )
}

/**
 * Isolated wrapper that collects restTimerState locally, preventing
 * the parent ActiveWorkoutScreen from recomposing every second while
 * the rest timer counts down.
 */
@Composable
private fun IsolatedRestTimerOverlay(viewModel: ActiveWorkoutViewModel) {
    val restTimerState by viewModel.restTimerState.collectAsStateWithLifecycle()
    RestTimerOverlay(
        timerState = restTimerState,
        onAdjustTime = { seconds -> viewModel.addRestTime(seconds) },
        onSkip = { viewModel.stopRestTimer() }
    )
}
