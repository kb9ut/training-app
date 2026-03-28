package com.kb9ut.pror.ui.screen.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.MuscleGroup
import com.kb9ut.pror.domain.repository.ExerciseRepository
import com.kb9ut.pror.domain.repository.WorkoutRepository
import com.kb9ut.pror.util.OneRepMaxCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

enum class ProgressPeriod {
    ONE_MONTH, THREE_MONTHS, SIX_MONTHS, ONE_YEAR, ALL
}

data class ProgressDataPoint(
    val date: Long,
    val estimated1RM: Double,
    val maxWeight: Double,
    val totalVolume: Double,
    val best1RMWeight: Double = 0.0,
    val best1RMReps: Int = 0
)

data class PersonalRecord(
    val value: Double,
    val date: Long
)

data class ProgressUiState(
    val allExercises: List<ExerciseEntity> = emptyList(),
    val selectedExercise: ExerciseEntity? = null,
    val selectedPeriod: ProgressPeriod = ProgressPeriod.THREE_MONTHS,
    val progressData: List<ProgressDataPoint> = emptyList(),
    val bestWeight: PersonalRecord? = null,
    val best1RM: PersonalRecord? = null,
    val bestVolume: PersonalRecord? = null,
    val weeklyMuscleVolume: Map<MuscleGroup, Double> = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    private val _selectedPeriod = MutableStateFlow(ProgressPeriod.THREE_MONTHS)
    val selectedPeriod: StateFlow<ProgressPeriod> = _selectedPeriod.asStateFlow()

    private val allExercises = exerciseRepository.getExercisesWithData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val weeklyMuscleVolume = workoutRepository.getVolumeByMuscleGroupSince(
        System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
    ).map { volumes ->
        volumes.mapNotNull { mv ->
            try {
                MuscleGroup.valueOf(mv.muscleGroup) to mv.totalVolume
            } catch (_: IllegalArgumentException) {
                null
            }
        }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val completedSets = _selectedExerciseId.flatMapLatest { exerciseId ->
        if (exerciseId != null) {
            workoutRepository.getCompletedSetsForExerciseChronological(exerciseId)
        } else {
            flowOf(emptyList())
        }
    }

    private val allProgressData = completedSets.map { sets ->
        if (sets.isEmpty()) return@map emptyList()

        // Group sets by day (using completedAt date)
        val calendar = Calendar.getInstance()
        val setsByDay = sets
            .filter { it.completedAt != null && it.weightKg != null && it.reps != null && it.reps > 0 && it.weightKg > 0 }
            .groupBy { set ->
                calendar.timeInMillis = set.completedAt!!
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }

        setsByDay.map { (dayMillis, daySets) ->
            var best1RM = 0.0
            var maxWeight = 0.0
            var totalVolume = 0.0
            var best1RMWeight = 0.0
            var best1RMReps = 0

            for (set in daySets) {
                val w = set.weightKg ?: continue
                val r = set.reps ?: continue
                if (w <= 0 || r <= 0) continue

                val result = OneRepMaxCalculator.calculateAll(w, r)
                val estimated = result?.median ?: w
                if (estimated > best1RM) {
                    best1RM = estimated
                    best1RMWeight = w
                    best1RMReps = r
                }
                if (w > maxWeight) maxWeight = w
                totalVolume += w * r
            }

            ProgressDataPoint(
                date = dayMillis,
                estimated1RM = best1RM,
                maxWeight = maxWeight,
                totalVolume = totalVolume,
                best1RMWeight = best1RMWeight,
                best1RMReps = best1RMReps
            )
        }.sortedBy { it.date }
    }

    val uiState: StateFlow<ProgressUiState> = combine(
        allExercises,
        _selectedExerciseId,
        _selectedPeriod,
        allProgressData,
        weeklyMuscleVolume
    ) { exercises, selectedId, period, allData, muscleVolume ->
        val selectedExercise = exercises.find { it.id == selectedId }

        val cutoff = getCutoffTime(period)
        val filteredData = if (cutoff != null) {
            allData.filter { it.date >= cutoff }
        } else {
            allData
        }

        val bestWeight = if (allData.isNotEmpty()) {
            allData.maxByOrNull { it.maxWeight }?.let {
                PersonalRecord(it.maxWeight, it.date)
            }
        } else null

        val best1RM = if (allData.isNotEmpty()) {
            allData.maxByOrNull { it.estimated1RM }?.let {
                PersonalRecord(it.estimated1RM, it.date)
            }
        } else null

        val bestVolume = if (allData.isNotEmpty()) {
            allData.maxByOrNull { it.totalVolume }?.let {
                PersonalRecord(it.totalVolume, it.date)
            }
        } else null

        ProgressUiState(
            allExercises = exercises,
            selectedExercise = selectedExercise,
            selectedPeriod = period,
            progressData = filteredData,
            bestWeight = bestWeight,
            best1RM = best1RM,
            bestVolume = bestVolume,
            weeklyMuscleVolume = muscleVolume
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressUiState())

    fun selectExercise(exerciseId: Long) {
        _selectedExerciseId.value = exerciseId
    }

    fun selectPeriod(period: ProgressPeriod) {
        _selectedPeriod.value = period
    }

    private fun getCutoffTime(period: ProgressPeriod): Long? {
        if (period == ProgressPeriod.ALL) return null
        val calendar = Calendar.getInstance()
        when (period) {
            ProgressPeriod.ONE_MONTH -> calendar.add(Calendar.MONTH, -1)
            ProgressPeriod.THREE_MONTHS -> calendar.add(Calendar.MONTH, -3)
            ProgressPeriod.SIX_MONTHS -> calendar.add(Calendar.MONTH, -6)
            ProgressPeriod.ONE_YEAR -> calendar.add(Calendar.YEAR, -1)
            ProgressPeriod.ALL -> { /* unreachable */ }
        }
        return calendar.timeInMillis
    }
}
