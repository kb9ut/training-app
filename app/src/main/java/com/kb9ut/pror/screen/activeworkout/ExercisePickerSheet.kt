package com.kb9ut.pror.ui.screen.activeworkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kb9ut.pror.R
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.MuscleGroup
import com.kb9ut.pror.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(
    exerciseRepository: ExerciseRepository,
    onExerciseSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf<MuscleGroup?>(null) }

    val exercisesFlow: Flow<List<ExerciseEntity>> = remember(searchQuery, selectedMuscleGroup) {
        when {
            searchQuery.isNotBlank() -> exerciseRepository.searchExercises(searchQuery)
            selectedMuscleGroup != null -> exerciseRepository.getExercisesByMuscleGroup(selectedMuscleGroup!!)
            else -> exerciseRepository.getAllExercises()
        }
    }

    val exercises by exercisesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.select_exercise),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_exercises)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_search)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMuscleGroup == null,
                    onClick = { selectedMuscleGroup = null },
                    label = { Text(stringResource(R.string.filter_all)) }
                )
                MuscleGroup.entries.forEach { muscleGroup ->
                    FilterChip(
                        selected = selectedMuscleGroup == muscleGroup,
                        onClick = {
                            selectedMuscleGroup = if (selectedMuscleGroup == muscleGroup) null else muscleGroup
                        },
                        label = { Text(muscleGroupDisplayName(muscleGroup)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                items(exercises, key = { it.id }) { exercise ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onExerciseSelected(exercise.id)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = muscleGroupDisplayName(exercise.muscleGroup),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun muscleGroupDisplayName(muscleGroup: MuscleGroup): String {
    return when (muscleGroup) {
        MuscleGroup.CHEST -> stringResource(R.string.muscle_chest)
        MuscleGroup.BACK -> stringResource(R.string.muscle_back)
        MuscleGroup.SHOULDERS -> stringResource(R.string.muscle_shoulders)
        MuscleGroup.BICEPS -> stringResource(R.string.muscle_biceps)
        MuscleGroup.TRICEPS -> stringResource(R.string.muscle_triceps)
        MuscleGroup.FOREARMS -> stringResource(R.string.muscle_forearms)
        MuscleGroup.QUADRICEPS -> stringResource(R.string.muscle_quadriceps)
        MuscleGroup.HAMSTRINGS -> stringResource(R.string.muscle_hamstrings)
        MuscleGroup.GLUTES -> stringResource(R.string.muscle_glutes)
        MuscleGroup.CALVES -> stringResource(R.string.muscle_calves)
        MuscleGroup.ABS -> stringResource(R.string.muscle_abs)
        MuscleGroup.OBLIQUES -> stringResource(R.string.muscle_obliques)
        MuscleGroup.TRAPS -> stringResource(R.string.muscle_traps)
        MuscleGroup.LATS -> stringResource(R.string.muscle_lats)
        MuscleGroup.FULL_BODY -> stringResource(R.string.muscle_full_body)
        MuscleGroup.CARDIO -> stringResource(R.string.muscle_cardio)
        MuscleGroup.OTHER -> stringResource(R.string.muscle_other)
    }
}
