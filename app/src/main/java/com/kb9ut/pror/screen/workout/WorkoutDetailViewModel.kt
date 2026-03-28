package com.kb9ut.pror.ui.screen.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity
import com.kb9ut.pror.data.local.dao.SetDao
import com.kb9ut.pror.data.local.dao.WorkoutExerciseDao
import com.kb9ut.pror.domain.repository.ExerciseRepository
import com.kb9ut.pror.domain.repository.WorkoutRepository
import com.kb9ut.pror.util.OneRepMaxCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseDetailInfo(
    val exercise: ExerciseEntity,
    val completedSets: List<WorkoutSetEntity>,
    val estimated1RM: Double?,
    val bestSetIndex: Int?
)

data class WorkoutDetailUiState(
    val workout: WorkoutEntity? = null,
    val exercises: List<ExerciseDetailInfo> = emptyList(),
    val totalVolume: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])

    private val _uiState = MutableStateFlow(WorkoutDetailUiState())
    val uiState: StateFlow<WorkoutDetailUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    init {
        loadWorkoutDetail()
    }

    private fun loadWorkoutDetail() {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(workoutId)
            if (workout == null) {
                _uiState.value = WorkoutDetailUiState(isLoading = false)
                return@launch
            }

            val weList = workoutExerciseDao.getExercisesForWorkoutOnce(workoutId)
            val exerciseDetails = weList.mapNotNull { we ->
                val exercise = exerciseRepository.getExerciseById(we.exerciseId) ?: return@mapNotNull null
                val completedSets = setDao.getCompletedSetsForWorkoutExerciseOnce(we.id)

                // Find best set by estimated 1RM
                var bestEstimated1RM: Double? = null
                var bestSetIdx: Int? = null
                completedSets.forEachIndexed { index, set ->
                    if (set.weightKg != null && set.reps != null && set.weightKg > 0 && set.reps > 0) {
                        val result = OneRepMaxCalculator.calculateAll(set.weightKg, set.reps)
                        if (result != null) {
                            val median = result.median
                            if (bestEstimated1RM == null || median > bestEstimated1RM!!) {
                                bestEstimated1RM = median
                                bestSetIdx = index
                            }
                        }
                    }
                }

                ExerciseDetailInfo(
                    exercise = exercise,
                    completedSets = completedSets,
                    estimated1RM = bestEstimated1RM,
                    bestSetIndex = bestSetIdx
                )
            }

            val totalVolume = exerciseDetails.sumOf { detail ->
                detail.completedSets.sumOf { set ->
                    val w = set.weightKg ?: 0.0
                    val r = set.reps ?: 0
                    w * r
                }
            }

            _uiState.value = WorkoutDetailUiState(
                workout = workout,
                exercises = exerciseDetails,
                totalVolume = totalVolume,
                isLoading = false
            )
        }
    }

    fun deleteWorkout() {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            _navigateBack.emit(Unit)
        }
    }
}
