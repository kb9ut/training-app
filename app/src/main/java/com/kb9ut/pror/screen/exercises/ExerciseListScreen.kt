package com.kb9ut.pror.ui.screen.exercises

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kb9ut.pror.R
import com.kb9ut.pror.ui.theme.Accent
import com.kb9ut.pror.ui.theme.BrushedSteel
import com.kb9ut.pror.ui.theme.GlassBackgroundElevated
import com.kb9ut.pror.ui.theme.GlassBorder
import com.kb9ut.pror.ui.theme.OnAccent
import com.kb9ut.pror.data.local.entity.EquipmentCategory
import com.kb9ut.pror.data.local.entity.MuscleGroup
import com.kb9ut.pror.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    navController: NavController,
    viewModel: ExercisesViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val selectedMuscleGroup by viewModel.selectedMuscleGroup.collectAsStateWithLifecycle()
    val selectedEquipment by viewModel.selectedEquipment.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    if (showAddDialog) {
        AddExerciseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, muscleGroup, secondaryMuscleGroup, equipmentCategory, notes ->
                viewModel.createExercise(name, muscleGroup, secondaryMuscleGroup, equipmentCategory, notes)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_exercises)) },
                windowInsets = WindowInsets(0)
            )

            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            viewModel.onSearchQueryChanged(it)
                        },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text(stringResource(R.string.search_exercises)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_search)) }
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {}

            // Muscle Group filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMuscleGroup == null,
                    onClick = { viewModel.onMuscleGroupSelected(null) },
                    label = { Text(stringResource(R.string.filter_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent,
                        selectedLabelColor = OnAccent
                    )
                )
                MuscleGroup.entries.forEach { group ->
                    FilterChip(
                        selected = selectedMuscleGroup == group,
                        onClick = {
                            viewModel.onMuscleGroupSelected(
                                if (selectedMuscleGroup == group) null else group
                            )
                        },
                        label = {
                            Text(
                                group.name.lowercase().replace('_', ' ')
                                    .replaceFirstChar { it.uppercase() }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = OnAccent
                        )
                    )
                }
            }

            // Equipment filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedEquipment == null,
                    onClick = { viewModel.onEquipmentSelected(null) },
                    label = { Text(stringResource(R.string.filter_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent,
                        selectedLabelColor = OnAccent
                    )
                )
                EquipmentCategory.entries.forEach { equip ->
                    FilterChip(
                        selected = selectedEquipment == equip,
                        onClick = {
                            viewModel.onEquipmentSelected(
                                if (selectedEquipment == equip) null else equip
                            )
                        },
                        label = {
                            Text(
                                equip.name.lowercase().replace('_', ' ')
                                    .replaceFirstChar { it.uppercase() }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = OnAccent
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exercises, key = { it.id }) { exercise ->
                    Card(
                        onClick = {
                            navController.navigate(Screen.ExerciseDetail.createRoute(exercise.id))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GlassBackgroundElevated
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = exercise.muscleGroup.name.lowercase()
                                            .replace('_', ' ')
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    exercise.secondaryMuscleGroup?.let { secondary ->
                                        Text(
                                            text = "/ ${
                                                secondary.name.lowercase()
                                                    .replace('_', ' ')
                                                    .replaceFirstChar { it.uppercase() }
                                            }",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = exercise.equipmentCategory.name.lowercase()
                                        .replace('_', ' ')
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = BrushedSteel
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Accent,
            contentColor = OnAccent,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_exercise)
            )
        }
    }
}
