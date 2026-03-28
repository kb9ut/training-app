package com.kb9ut.pror.ui.screen.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kb9ut.pror.R
import com.kb9ut.pror.data.local.entity.EquipmentCategory
import com.kb9ut.pror.data.local.entity.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        muscleGroup: MuscleGroup,
        secondaryMuscleGroup: MuscleGroup?,
        equipmentCategory: EquipmentCategory,
        notes: String?
    ) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var selectedMuscleGroup by remember { mutableStateOf(MuscleGroup.CHEST) }
    var selectedSecondaryMuscleGroup by remember { mutableStateOf<MuscleGroup?>(null) }
    var selectedEquipment by remember { mutableStateOf(EquipmentCategory.BARBELL) }
    var notes by rememberSaveable { mutableStateOf("") }

    var muscleGroupExpanded by remember { mutableStateOf(false) }
    var secondaryMuscleGroupExpanded by remember { mutableStateOf(false) }
    var equipmentExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_exercise)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text(stringResource(R.string.exercise_name)) },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text(stringResource(R.string.exercise_name_required)) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Muscle Group dropdown
                ExposedDropdownMenuBox(
                    expanded = muscleGroupExpanded,
                    onExpandedChange = { muscleGroupExpanded = it }
                ) {
                    OutlinedTextField(
                        value = muscleGroupDisplayName(selectedMuscleGroup),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.muscle_group)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleGroupExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = muscleGroupExpanded,
                        onDismissRequest = { muscleGroupExpanded = false }
                    ) {
                        MuscleGroup.entries.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(muscleGroupDisplayName(group)) },
                                onClick = {
                                    selectedMuscleGroup = group
                                    muscleGroupExpanded = false
                                }
                            )
                        }
                    }
                }

                // Secondary Muscle Group dropdown
                ExposedDropdownMenuBox(
                    expanded = secondaryMuscleGroupExpanded,
                    onExpandedChange = { secondaryMuscleGroupExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedSecondaryMuscleGroup != null) {
                            muscleGroupDisplayName(selectedSecondaryMuscleGroup!!)
                        } else {
                            stringResource(R.string.none)
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.secondary_muscle_group)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = secondaryMuscleGroupExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = secondaryMuscleGroupExpanded,
                        onDismissRequest = { secondaryMuscleGroupExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.none)) },
                            onClick = {
                                selectedSecondaryMuscleGroup = null
                                secondaryMuscleGroupExpanded = false
                            }
                        )
                        MuscleGroup.entries.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(muscleGroupDisplayName(group)) },
                                onClick = {
                                    selectedSecondaryMuscleGroup = group
                                    secondaryMuscleGroupExpanded = false
                                }
                            )
                        }
                    }
                }

                // Equipment dropdown
                ExposedDropdownMenuBox(
                    expanded = equipmentExpanded,
                    onExpandedChange = { equipmentExpanded = it }
                ) {
                    OutlinedTextField(
                        value = equipmentDisplayName(selectedEquipment),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.equipment)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipmentExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = equipmentExpanded,
                        onDismissRequest = { equipmentExpanded = false }
                    ) {
                        EquipmentCategory.entries.forEach { equip ->
                            DropdownMenuItem(
                                text = { Text(equipmentDisplayName(equip)) },
                                onClick = {
                                    selectedEquipment = equip
                                    equipmentExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onConfirm(
                            name.trim(),
                            selectedMuscleGroup,
                            selectedSecondaryMuscleGroup,
                            selectedEquipment,
                            notes.takeIf { it.isNotBlank() }
                        )
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun muscleGroupDisplayName(group: MuscleGroup): String =
    group.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

private fun equipmentDisplayName(equip: EquipmentCategory): String =
    equip.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
