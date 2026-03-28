package com.kb9ut.pror.ui.screen.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.local.entity.EquipmentCategory
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.MuscleGroup
import com.kb9ut.pror.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val _selectedMuscleGroup = MutableStateFlow<MuscleGroup?>(null)
    val selectedMuscleGroup: StateFlow<MuscleGroup?> = _selectedMuscleGroup.asStateFlow()

    private val _selectedEquipment = MutableStateFlow<EquipmentCategory?>(null)
    val selectedEquipment: StateFlow<EquipmentCategory?> = _selectedEquipment.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: StateFlow<List<ExerciseEntity>> =
        combine(searchQuery, _selectedMuscleGroup, _selectedEquipment) { query, muscle, equip ->
            Triple(query, muscle, equip)
        }.flatMapLatest { (query, muscle, _) ->
            val baseFlow = if (query.isBlank()) {
                if (muscle != null) {
                    exerciseRepository.getExercisesByMuscleGroup(muscle)
                } else {
                    exerciseRepository.getAllExercises()
                }
            } else {
                exerciseRepository.searchExercises(query)
            }
            baseFlow
        }.combine(_selectedMuscleGroup) { list, muscle ->
            if (muscle != null && searchQuery.value.isNotBlank()) {
                list.filter { it.muscleGroup == muscle }
            } else {
                list
            }
        }.combine(_selectedEquipment) { list, equip ->
            if (equip != null) {
                list.filter { it.equipmentCategory == equip }
            } else {
                list
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onMuscleGroupSelected(muscleGroup: MuscleGroup?) {
        _selectedMuscleGroup.value = muscleGroup
    }

    fun onEquipmentSelected(equipment: EquipmentCategory?) {
        _selectedEquipment.value = equipment
    }

    fun createExercise(
        name: String,
        muscleGroup: MuscleGroup,
        secondaryMuscleGroup: MuscleGroup?,
        equipmentCategory: EquipmentCategory,
        notes: String?
    ) {
        viewModelScope.launch {
            exerciseRepository.insertExercise(
                ExerciseEntity(
                    name = name,
                    muscleGroup = muscleGroup,
                    secondaryMuscleGroup = secondaryMuscleGroup,
                    equipmentCategory = equipmentCategory,
                    isCustom = true,
                    notes = notes?.takeIf { it.isNotBlank() }
                )
            )
        }
    }
}
