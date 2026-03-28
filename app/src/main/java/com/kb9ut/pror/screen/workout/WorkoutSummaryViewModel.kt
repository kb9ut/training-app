package com.kb9ut.pror.ui.screen.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.local.dao.SetDao
import com.kb9ut.pror.util.FormatUtils
import com.kb9ut.pror.data.local.dao.WorkoutExerciseDao
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.domain.repository.ExerciseRepository
import com.kb9ut.pror.domain.repository.WorkoutRepository
import com.kb9ut.pror.util.OneRepMaxCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExercisePrInfo(
    val exerciseName: String,
    val prLabel: String // e.g. "Weight PR: 100kg" or "Est. 1RM PR: 120kg"
)

data class WorkoutSummaryUiState(
    val workout: WorkoutEntity? = null,
    val durationSeconds: Int = 0,
    val totalVolume: Double = 0.0,
    val setsCompleted: Int = 0,
    val exerciseCount: Int = 0,
    val prs: List<ExercisePrInfo> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WorkoutSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])

    private val _uiState = MutableStateFlow(WorkoutSummaryUiState())
    val uiState: StateFlow<WorkoutSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(workoutId)
            if (workout == null) {
                _uiState.value = WorkoutSummaryUiState(isLoading = false)
                return@launch
            }

            val durationSeconds = workout.durationSeconds
                ?: if (workout.finishedAt != null && workout.startedAt > 0) {
                    ((workout.finishedAt - workout.startedAt) / 1000).toInt()
                } else {
                    0
                }

            val weList = workoutExerciseDao.getExercisesForWorkoutOnce(workoutId)

            var totalVolume = 0.0
            var setsCompleted = 0
            val prs = mutableListOf<ExercisePrInfo>()

            for (we in weList) {
                val exercise = exerciseRepository.getExerciseById(we.exerciseId) ?: continue
                val completedSets = setDao.getCompletedSetsForWorkoutExerciseOnce(we.id)
                setsCompleted += completedSets.size

                for (set in completedSets) {
                    val w = set.weightKg ?: 0.0
                    val r = set.reps ?: 0
                    totalVolume += w * r

                    // Check for weight PR
                    if (w > 0 && r > 0) {
                        val allSetsForExercise = workoutRepository.getCompletedSetsForExercise(we.exerciseId).first()
                        val otherSets = allSetsForExercise.filter { it.id != set.id }

                        val bestWeight = otherSets.mapNotNull { it.weightKg }.maxOrNull() ?: 0.0
                        if (w > bestWeight && !prs.any { it.exerciseName == exercise.name && it.prLabel.contains("Weight") }) {
                            prs.add(ExercisePrInfo(exercise.name, "Weight PR: ${formatWeight(w)}"))
                        }

                        // Check 1RM PR
                        val current1Rm = OneRepMaxCalculator.calculateAll(w, r)?.median
                        if (current1Rm != null) {
                            val best1Rm = otherSets.mapNotNull { s ->
                                val sw = s.weightKg ?: return@mapNotNull null
                                val sr = s.reps ?: return@mapNotNull null
                                if (sw > 0 && sr > 0) OneRepMaxCalculator.calculateAll(sw, sr)?.median else null
                            }.maxOrNull() ?: 0.0
                            if (current1Rm > best1Rm && !prs.any { it.exerciseName == exercise.name && it.prLabel.contains("1RM") }) {
                                prs.add(ExercisePrInfo(exercise.name, "Est. 1RM PR: ${formatWeight(current1Rm)}"))
                            }
                        }
                    }
                }
            }

            _uiState.value = WorkoutSummaryUiState(
                workout = workout,
                durationSeconds = durationSeconds,
                totalVolume = totalVolume,
                setsCompleted = setsCompleted,
                exerciseCount = weList.size,
                prs = prs,
                isLoading = false
            )
        }
    }

    private fun formatWeight(weight: Double): String {
        return "${FormatUtils.formatWeightValue(weight)}kg"
    }
}
