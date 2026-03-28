package com.kb9ut.pror.ui.screen.routine

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.components.AccentGlassCard
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.OnAccent
import com.kb9ut.pror.ui.theme.SurfaceBright
import com.kb9ut.pror.data.local.entity.RoutineEntity
import com.kb9ut.pror.data.local.seed.ProgramPreset
import com.kb9ut.pror.ui.navigation.Screen
import com.kb9ut.pror.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
    navController: NavController,
    viewModel: RoutineListViewModel = hiltViewModel()
) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    var routineToDelete by remember { mutableStateOf<RoutineEntity?>(null) }
    var showProgramBrowser by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigateToWorkout.collect { workoutId ->
            navController.navigate(Screen.ActiveWorkout.createRoute(workoutId))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.importResult.collect { success ->
            val message = if (success) {
                context.getString(R.string.program_imported)
            } else {
                context.getString(R.string.import_error)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text(stringResource(R.string.routines)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.RoutineEditor.createRoute(0L))
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.routine_new))
                    }
                }
            )

            if (routines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.routine_no_routines),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { showProgramBrowser = true }) {
                            Text(stringResource(R.string.browse_programs))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Browse Programs section
                    item {
                        Text(
                            text = stringResource(R.string.browse_programs_section).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = BrushedSteel,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AccentGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showProgramBrowser = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Explore,
                                    contentDescription = stringResource(R.string.browse_programs),
                                    tint = Accent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.browse_programs),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.browse_programs_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = BrushedSteel
                                )
                            }
                        }
                    }

                    // My Routines section header
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.my_routines_section).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = BrushedSteel,
                            letterSpacing = 1.5.sp
                        )
                    }

                    items(routines, key = { it.id }) { routine ->
                        RoutineCard(
                            routine = routine,
                            onStart = { viewModel.startWorkoutFromRoutine(routine.id) },
                            onEdit = {
                                navController.navigate(Screen.RoutineEditor.createRoute(routine.id))
                            },
                            onSwipeToDelete = { routineToDelete = routine }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    routineToDelete?.let { routine ->
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            title = { Text(stringResource(R.string.routine_delete_confirm)) },
            text = { Text(routine.name) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRoutine(routine.id)
                    routineToDelete = null
                }) {
                    Text(stringResource(R.string.done))
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showProgramBrowser) {
        ProgramBrowserSheet(
            presets = viewModel.programPresets,
            onImport = { preset ->
                val routineNames = preset.routines.map { context.getString(it.nameResId) }
                viewModel.importProgram(routineNames, preset)
                showProgramBrowser = false
            },
            onDismiss = { showProgramBrowser = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgramBrowserSheet(
    presets: List<ProgramPreset>,
    onImport: (ProgramPreset) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPreset by remember { mutableStateOf<ProgramPreset?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceBright
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.browse_programs),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (selectedPreset == null) {
                presets.forEach { preset ->
                    ProgramPresetCard(
                        preset = preset,
                        onClick = { selectedPreset = preset }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                val preset = selectedPreset!!
                ProgramDetailView(
                    preset = preset,
                    onImport = { onImport(preset) },
                    onBack = { selectedPreset = null }
                )
            }
        }
    }
}

@Composable
private fun ProgramPresetCard(
    preset: ProgramPreset,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(preset.nameResId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(preset.descriptionResId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${preset.routines.size} ${stringResource(R.string.routines).lowercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = BrushedSteel
            )
        }
    }
}

@Composable
private fun ProgramDetailView(
    preset: ProgramPreset,
    onImport: () -> Unit,
    onBack: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
            Text(
                text = stringResource(preset.nameResId),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(preset.descriptionResId),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        preset.routines.forEach { routine ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(routine.nameResId),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    routine.exercises.forEach { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = exercise.exerciseName,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${exercise.sets} x ${exercise.targetReps}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onImport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = OnAccent
            )
        ) {
            Text(stringResource(R.string.import_program))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineCard(
    routine: RoutineEntity,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onSwipeToDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeToDelete()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
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
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.routine_edit),
                            tint = BrushedSteel
                        )
                    }
                }

                routine.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                routine.lastUsedAt?.let { lastUsed ->
                    Text(
                        text = DateTimeUtils.formatDate(lastUsed),
                        style = MaterialTheme.typography.bodySmall,
                        color = BrushedSteel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = OnAccent
                    )
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.cd_start_routine),
                        tint = OnAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.routine_start))
                }
            }
        }
    }
}
