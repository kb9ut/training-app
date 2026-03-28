package com.kb9ut.pror.ui.screen.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.BrushedSteel

import com.kb9ut.pror.ui.screen.activeworkout.ExercisePickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    navController: NavController,
    viewModel: RoutineEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExercisePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.saveCompleted.collect {
            navController.popBackStack()
        }
    }

    val accentTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Accent,
        unfocusedBorderColor = BrushedSteel.copy(alpha = 0.5f),
        cursorColor = Accent,
        focusedLabelColor = Accent
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    if (viewModel.isNewRoutine)
                        stringResource(R.string.routine_new)
                    else
                        stringResource(R.string.routine_edit)
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close))
                }
            },
            actions = {
                TextButton(
                    onClick = { viewModel.saveRoutine() },
                    enabled = uiState.routineName.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.routineName,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text(stringResource(R.string.routine_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = accentTextFieldColors
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.routineDescription,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text(stringResource(R.string.routine_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp),
                    colors = accentTextFieldColors
                )
            }

            itemsIndexed(uiState.exercises) { exerciseIndex, entry ->
                ExerciseTemplateCard(
                    entry = entry,
                    exerciseIndex = exerciseIndex,
                    onRemoveExercise = { viewModel.removeExercise(exerciseIndex) },
                    onUpdateSet = { setIndex, reps, weight ->
                        viewModel.updateSetTemplate(exerciseIndex, setIndex, reps, weight)
                    },
                    onAddSet = { viewModel.addSetTemplate(exerciseIndex) },
                    onRemoveSet = { setIndex ->
                        viewModel.removeSetTemplate(exerciseIndex, setIndex)
                    },
                    onNotesChanged = { notes ->
                        viewModel.updateExerciseNotes(exerciseIndex, notes)
                    }
                )
            }

            item {
                OutlinedButton(
                    onClick = { showExercisePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Accent),
                        width = 1.dp
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Accent,
                        containerColor = Color.Transparent
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_exercise))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.add_exercise))
                }
            }
        }
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

@Composable
private fun ExerciseTemplateCard(
    entry: ExerciseEntryUiModel,
    exerciseIndex: Int,
    onRemoveExercise: () -> Unit,
    onUpdateSet: (setIndex: Int, reps: Int?, weight: Double?) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (setIndex: Int) -> Unit,
    onNotesChanged: (String) -> Unit
) {
    var showNotes by remember { mutableStateOf(!entry.notes.isNullOrBlank()) }

    val accentTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Accent,
        unfocusedBorderColor = BrushedSteel.copy(alpha = 0.5f),
        cursorColor = Accent,
        focusedLabelColor = Accent
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showNotes = !showNotes }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.StickyNote2,
                        contentDescription = stringResource(R.string.notes),
                        tint = if (!entry.notes.isNullOrBlank())
                            Accent
                        else
                            BrushedSteel
                    )
                }
                IconButton(onClick = onRemoveExercise) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.remove_exercise),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (showNotes) {
                OutlinedTextField(
                    value = entry.notes ?: "",
                    onValueChange = onNotesChanged,
                    placeholder = { Text(stringResource(R.string.routine_exercise_notes_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    minLines = 1,
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    colors = accentTextFieldColors
                )
            }

            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.set_number).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrushedSteel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    text = stringResource(R.string.routine_target_weight).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrushedSteel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.routine_target_reps).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = BrushedSteel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(40.dp))
            }

            entry.setTemplates.forEachIndexed { setIndex, setTemplate ->
                SetTemplateRow(
                    setNumber = setIndex + 1,
                    setTemplate = setTemplate,
                    onUpdate = { reps, weight -> onUpdateSet(setIndex, reps, weight) },
                    onRemove = { onRemoveSet(setIndex) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.routine_add_set_template),
                    color = Accent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetTemplateRow(
    setNumber: Int,
    setTemplate: SetTemplateUiModel,
    onUpdate: (reps: Int?, weight: Double?) -> Unit,
    onRemove: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else {
                false
            }
        }
    )

    val accentTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Accent,
        unfocusedBorderColor = BrushedSteel.copy(alpha = 0.5f),
        cursorColor = Accent,
        focusedLabelColor = Accent
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$setNumber",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(40.dp)
            )

            OutlinedTextField(
                value = setTemplate.targetWeightKg?.let {
                    if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                } ?: "",
                onValueChange = { text ->
                    val weight = text.toDoubleOrNull()
                    onUpdate(setTemplate.targetReps, weight)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(8.dp),
                colors = accentTextFieldColors
            )

            OutlinedTextField(
                value = setTemplate.targetReps?.toString() ?: "",
                onValueChange = { text ->
                    val reps = text.toIntOrNull()
                    onUpdate(reps, setTemplate.targetWeightKg)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(8.dp),
                colors = accentTextFieldColors
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.width(40.dp)
            ) {
                Icon(
                    Icons.Filled.RemoveCircleOutline,
                    contentDescription = stringResource(R.string.cd_remove_set),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
