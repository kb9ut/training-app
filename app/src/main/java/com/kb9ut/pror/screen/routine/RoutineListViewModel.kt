package com.kb9ut.pror.ui.screen.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.local.entity.RoutineEntity
import com.kb9ut.pror.data.local.entity.RoutineExerciseEntity
import com.kb9ut.pror.data.local.entity.RoutineSetTemplateEntity
import com.kb9ut.pror.data.local.seed.ProgramPreset
import com.kb9ut.pror.data.local.seed.ProgramPresets
import com.kb9ut.pror.domain.repository.ExerciseRepository
import com.kb9ut.pror.domain.repository.RoutineRepository
import com.kb9ut.pror.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineListViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    val routines: StateFlow<List<RoutineEntity>> =
        routineRepository.getAllRoutines()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _navigateToWorkout = MutableSharedFlow<Long>()
    val navigateToWorkout: SharedFlow<Long> = _navigateToWorkout.asSharedFlow()

    private val _importResult = MutableSharedFlow<Boolean>()
    val importResult: SharedFlow<Boolean> = _importResult.asSharedFlow()

    val programPresets: List<ProgramPreset> = ProgramPresets.getAll()

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
        }
    }

    /**
     * Import a program preset. Routine names are resolved from string resources
     * on the UI side and passed as [routineNames].
     */
    fun importProgram(routineNames: List<String>, preset: ProgramPreset) {
        viewModelScope.launch {
            try {
                for ((index, routinePreset) in preset.routines.withIndex()) {
                    val routineName = routineNames.getOrElse(index) { "Routine ${index + 1}" }

                    val routineId = routineRepository.insertRoutine(
                        RoutineEntity(name = routineName)
                    )

                    val exerciseEntities = routinePreset.exercises.mapIndexedNotNull { exIndex, exercisePreset ->
                        val exercise = exerciseRepository.getExerciseByName(exercisePreset.exerciseName)
                        if (exercise != null) {
                            Triple(exIndex, exercise.id, exercisePreset)
                        } else {
                            null
                        }
                    }

                    for ((orderIndex, exerciseId, exercisePreset) in exerciseEntities) {
                        val routineExerciseId = routineRepository.insertRoutineExercise(
                            RoutineExerciseEntity(
                                routineId = routineId,
                                exerciseId = exerciseId,
                                orderIndex = orderIndex
                            )
                        )

                        val setTemplates = (0 until exercisePreset.sets).map { setIndex ->
                            RoutineSetTemplateEntity(
                                routineExerciseId = routineExerciseId,
                                orderIndex = setIndex,
                                targetReps = exercisePreset.targetReps
                            )
                        }
                        routineRepository.saveRoutineSetTemplates(routineExerciseId, setTemplates)
                    }
                }
                _importResult.emit(true)
            } catch (_: Exception) {
                _importResult.emit(false)
            }
        }
    }

    fun startWorkoutFromRoutine(routineId: Long) {
        viewModelScope.launch {
            val routine = routineRepository.getRoutineById(routineId) ?: return@launch
            val workoutId = workoutRepository.startWorkout(
                name = routine.name,
                routineId = routineId
            )

            val routineExercises = routineRepository.getRoutineExercises(routineId)
            for ((index, routineExercise) in routineExercises.withIndex()) {
                val workoutExerciseId = workoutRepository.addExerciseToWorkout(
                    workoutId = workoutId,
                    exerciseId = routineExercise.exerciseId,
                    orderIndex = index,
                    notes = routineExercise.notes
                )

                val templates = routineRepository.getRoutineSetTemplates(routineExercise.id)
                for ((setIndex, template) in templates.withIndex()) {
                    val setId = workoutRepository.addSet(workoutExerciseId, setIndex)
                    workoutRepository.updateSet(
                        com.kb9ut.pror.data.local.entity.WorkoutSetEntity(
                            id = setId,
                            workoutExerciseId = workoutExerciseId,
                            orderIndex = setIndex,
                            weightKg = template.targetWeightKg,
                            reps = template.targetReps,
                            setType = template.setType
                        )
                    )
                }
            }

            routineRepository.updateRoutine(routine.copy(lastUsedAt = System.currentTimeMillis()))

            _navigateToWorkout.emit(workoutId)
        }
    }
}
