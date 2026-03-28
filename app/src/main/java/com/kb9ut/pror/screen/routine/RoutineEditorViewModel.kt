package com.kb9ut.pror.ui.screen.routine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.RoutineExerciseEntity
import com.kb9ut.pror.data.local.entity.RoutineSetTemplateEntity
import com.kb9ut.pror.data.local.entity.SetType
import com.kb9ut.pror.domain.repository.ExerciseRepository
import com.kb9ut.pror.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetTemplateUiModel(
    val targetReps: Int? = null,
    val targetWeightKg: Double? = null,
    val setType: SetType = SetType.NORMAL
)

data class ExerciseEntryUiModel(
    val exercise: ExerciseEntity,
    val setTemplates: List<SetTemplateUiModel>,
    val notes: String? = null
)

data class RoutineEditorUiState(
    val routineName: String = "",
    val routineDescription: String = "",
    val exercises: List<ExerciseEntryUiModel> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RoutineEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    val exerciseRepository: ExerciseRepository
) : ViewModel() {

    val routineId: Long = savedStateHandle.get<Long>("routineId") ?: 0L
    val isNewRoutine: Boolean get() = routineId == 0L

    private val _uiState = MutableStateFlow(RoutineEditorUiState())
    val uiState: StateFlow<RoutineEditorUiState> = _uiState.asStateFlow()

    private val _saveCompleted = MutableSharedFlow<Unit>()
    val saveCompleted: SharedFlow<Unit> = _saveCompleted.asSharedFlow()

    init {
        if (routineId > 0L) {
            loadRoutine()
        }
    }

    private fun loadRoutine() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val routine = routineRepository.getRoutineById(routineId) ?: return@launch
            val routineExercises = routineRepository.getRoutineExercises(routineId)

            val exerciseEntries = routineExercises.mapNotNull { routineExercise ->
                val exercise = exerciseRepository.getExerciseById(routineExercise.exerciseId)
                    ?: return@mapNotNull null
                val templates = routineRepository.getRoutineSetTemplates(routineExercise.id)
                ExerciseEntryUiModel(
                    exercise = exercise,
                    setTemplates = templates.map { template ->
                        SetTemplateUiModel(
                            targetReps = template.targetReps,
                            targetWeightKg = template.targetWeightKg,
                            setType = template.setType
                        )
                    },
                    notes = routineExercise.notes
                )
            }

            _uiState.update {
                it.copy(
                    routineName = routine.name,
                    routineDescription = routine.description ?: "",
                    exercises = exerciseEntries,
                    isLoading = false
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(routineName = name) }
    }

    fun updateDescription(desc: String) {
        _uiState.update { it.copy(routineDescription = desc) }
    }

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExerciseById(exerciseId) ?: return@launch
            val defaultSets = List(3) { SetTemplateUiModel() }
            _uiState.update {
                it.copy(exercises = it.exercises + ExerciseEntryUiModel(exercise, defaultSets))
            }
        }
    }

    fun updateExerciseNotes(exerciseIndex: Int, notes: String) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            if (exerciseIndex in exercises.indices) {
                exercises[exerciseIndex] = exercises[exerciseIndex].copy(notes = notes)
            }
            state.copy(exercises = exercises)
        }
    }

    fun removeExercise(index: Int) {
        _uiState.update {
            it.copy(exercises = it.exercises.toMutableList().apply { removeAt(index) })
        }
    }

    fun moveExercise(from: Int, to: Int) {
        _uiState.update { state ->
            val list = state.exercises.toMutableList()
            if (from in list.indices && to in list.indices) {
                val item = list.removeAt(from)
                list.add(to, item)
            }
            state.copy(exercises = list)
        }
    }

    fun updateSetTemplate(exerciseIndex: Int, setIndex: Int, reps: Int?, weight: Double?) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            if (exerciseIndex in exercises.indices) {
                val entry = exercises[exerciseIndex]
                val sets = entry.setTemplates.toMutableList()
                if (setIndex in sets.indices) {
                    sets[setIndex] = sets[setIndex].copy(targetReps = reps, targetWeightKg = weight)
                    exercises[exerciseIndex] = entry.copy(setTemplates = sets)
                }
            }
            state.copy(exercises = exercises)
        }
    }

    fun addSetTemplate(exerciseIndex: Int) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            if (exerciseIndex in exercises.indices) {
                val entry = exercises[exerciseIndex]
                exercises[exerciseIndex] = entry.copy(
                    setTemplates = entry.setTemplates + SetTemplateUiModel()
                )
            }
            state.copy(exercises = exercises)
        }
    }

    fun removeSetTemplate(exerciseIndex: Int, setIndex: Int) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            if (exerciseIndex in exercises.indices) {
                val entry = exercises[exerciseIndex]
                val sets = entry.setTemplates.toMutableList()
                if (setIndex in sets.indices) {
                    sets.removeAt(setIndex)
                    exercises[exerciseIndex] = entry.copy(setTemplates = sets)
                }
            }
            state.copy(exercises = exercises)
        }
    }

    fun saveRoutine() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.routineName.isBlank()) return@launch

            val savedRoutineId = if (isNewRoutine) {
                routineRepository.insertRoutine(
                    com.kb9ut.pror.data.local.entity.RoutineEntity(
                        name = state.routineName,
                        description = state.routineDescription.takeIf { it.isNotBlank() }
                    )
                )
            } else {
                val existing = routineRepository.getRoutineById(routineId) ?: return@launch
                routineRepository.updateRoutine(
                    existing.copy(
                        name = state.routineName,
                        description = state.routineDescription.takeIf { it.isNotBlank() }
                    )
                )
                routineId
            }

            // Save exercises - delete old ones first handled by saveRoutineExercises
            routineRepository.saveRoutineExercises(
                savedRoutineId,
                state.exercises.mapIndexed { index, entry ->
                    RoutineExerciseEntity(
                        routineId = savedRoutineId,
                        exerciseId = entry.exercise.id,
                        orderIndex = index,
                        notes = entry.notes?.takeIf { it.isNotBlank() }
                    )
                }
            )

            // Now get the saved exercise IDs to link set templates
            val savedExercises = routineRepository.getRoutineExercises(savedRoutineId)
            for ((index, routineExercise) in savedExercises.withIndex()) {
                if (index < state.exercises.size) {
                    val templates = state.exercises[index].setTemplates.mapIndexed { setIndex, set ->
                        RoutineSetTemplateEntity(
                            routineExerciseId = routineExercise.id,
                            orderIndex = setIndex,
                            targetReps = set.targetReps,
                            targetWeightKg = set.targetWeightKg,
                            setType = set.setType
                        )
                    }
                    routineRepository.saveRoutineSetTemplates(routineExercise.id, templates)
                }
            }

            _saveCompleted.emit(Unit)
        }
    }
}
