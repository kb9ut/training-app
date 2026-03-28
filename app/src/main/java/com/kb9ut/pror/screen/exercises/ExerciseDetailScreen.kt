package com.kb9ut.pror.ui.screen.exercises

import com.kb9ut.pror.util.FormatUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.components.GlassCard
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.DarkSteel
import com.kb9ut.pror.ui.theme.GlassBackgroundElevated
import com.kb9ut.pror.ui.theme.GlassBorder
import com.kb9ut.pror.ui.theme.GoldPR
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    viewModel: ExerciseDetailViewModel = hiltViewModel()
) {
    val exercise by viewModel.exercise.collectAsStateWithLifecycle()
    val personalBests by viewModel.personalBests.collectAsStateWithLifecycle()
    val setHistory by viewModel.setHistory.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val successMessage = stringResource(R.string.csv_export_success)
    val errorMessage = stringResource(R.string.csv_export_error)

    LaunchedEffect(Unit) {
        viewModel.csvExportResult.collect { result ->
            when (result) {
                is CsvExportResult.Success -> snackbarHostState.showSnackbar(successMessage)
                is CsvExportResult.Error -> snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.exportToCsv(it) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "") },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val exerciseName = exercise?.name?.replace(" ", "_") ?: "exercise"
                            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                                .format(Date())
                            csvExportLauncher.launch("reppen_${exerciseName}_${dateStr}.csv")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = stringResource(R.string.export_csv)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exercise Info Card
            exercise?.let { ex ->
                item(key = "info") {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = ex.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = ex.muscleGroup.name.lowercase()
                                        .replace('_', ' ')
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            DarkSteel,
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                                ex.secondaryMuscleGroup?.let { secondary ->
                                    Text(
                                        text = secondary.name.lowercase()
                                            .replace('_', ' ')
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .border(
                                                1.dp,
                                                DarkSteel,
                                                RoundedCornerShape(50)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = ex.equipmentCategory.name.lowercase()
                                        .replace('_', ' ')
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            DarkSteel,
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            ex.notes?.let { notes ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${stringResource(R.string.notes)}: $notes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Personal Bests section
            item(key = "pb_header") {
                Text(
                    text = stringResource(R.string.personal_bests).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = BrushedSteel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item(key = "pb_content") {
                val pb = personalBests
                if (pb.maxWeight == null && pb.maxReps == null && pb.estimated1RM == null) {
                    Text(
                        text = stringResource(R.string.no_personal_bests),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pb.maxWeight?.let { weight ->
                                val kgLabel = stringResource(R.string.kg)
                                val repsLabel = pb.maxWeightReps?.toString() ?: "-"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.max_weight),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${FormatUtils.formatWeightValue(weight)} $kgLabel x $repsLabel",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldPR
                                    )
                                }
                            }
                            pb.maxReps?.let { reps ->
                                val kgLabel = stringResource(R.string.kg)
                                val repsLabel = stringResource(R.string.reps)
                                val weightStr = pb.maxRepsWeight?.let { FormatUtils.formatWeightValue(it) } ?: "-"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.max_reps),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "$reps $repsLabel @ $weightStr $kgLabel",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldPR
                                    )
                                }
                            }
                            pb.estimated1RM?.let { e1rm ->
                                val kgLabel = stringResource(R.string.kg)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.estimated_1rm),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${"%.1f".format(e1rm)} $kgLabel",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldPR
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // History section
            item(key = "history_header") {
                Text(
                    text = stringResource(R.string.history).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = BrushedSteel,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (setHistory.isEmpty()) {
                item(key = "history_empty") {
                    Text(
                        text = stringResource(R.string.no_history_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Group by date (day)
                val dayFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                val grouped = setHistory.groupBy { entry ->
                    dayFormat.format(Date(entry.completedAt))
                }

                grouped.forEach { (dateString, entries) ->
                    item(key = "date_$dateString") {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Show notes once per workout exercise in each date group
                    val shownNotes = mutableSetOf<Long>()
                    entries.forEach { entry ->
                        val weId = entry.set.workoutExerciseId
                        val notes = entry.workoutExerciseNotes
                        if (!notes.isNullOrBlank() && shownNotes.add(weId)) {
                            item(key = "notes_${dateString}_$weId") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.StickyNote2,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(
                        items = entries,
                        key = { "set_${it.set.id}" }
                    ) { entry ->
                        val kgLabel = stringResource(R.string.kg)
                        val rpeLabel = stringResource(R.string.rpe)
                        val weightStr = entry.set.weightKg?.let { FormatUtils.formatWeightValue(it) } ?: "-"
                        val repsStr = entry.set.reps?.toString() ?: "-"
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$weightStr $kgLabel x $repsStr",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                entry.set.rpe?.let { rpe ->
                                    Text(
                                        text = "$rpeLabel ${"%.1f".format(rpe)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
