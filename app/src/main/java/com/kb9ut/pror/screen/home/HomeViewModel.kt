package com.kb9ut.pror.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.local.entity.RoutineEntity
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.domain.repository.RoutineRepository
import com.kb9ut.pror.domain.repository.WorkoutRepository
import com.kb9ut.pror.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    val recentWorkouts: StateFlow<List<WorkoutEntity>> =
        workoutRepository.getRecentWorkouts(3)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeWorkout: StateFlow<WorkoutEntity?> =
        workoutRepository.observeActiveWorkout()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val thisWeekWorkoutCount: StateFlow<Int> = run {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        val startMillis = DateTimeUtils.startOfDay(monday)
        val endMillis = DateTimeUtils.endOfDay(today)
        workoutRepository.getWorkoutDatesInRange(startMillis, endMillis)
            .map { dates -> dates.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    val currentStreak: StateFlow<Int> = run {
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30)
        val startMillis = DateTimeUtils.startOfDay(thirtyDaysAgo)
        val endMillis = DateTimeUtils.endOfDay(today)
        workoutRepository.getWorkoutDatesInRange(startMillis, endMillis)
            .map { timestamps -> calculateStreak(timestamps, today) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    val routines: StateFlow<List<RoutineEntity>> =
        routineRepository.getAllRoutines()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _navigateToWorkout = MutableSharedFlow<Long>()
    val navigateToWorkout: SharedFlow<Long> = _navigateToWorkout.asSharedFlow()

    fun startEmptyWorkout() {
        viewModelScope.launch {
            val workoutId = workoutRepository.startWorkout("Workout")
            _navigateToWorkout.emit(workoutId)
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

    private fun calculateStreak(timestamps: List<Long>, today: LocalDate): Int {
        if (timestamps.isEmpty()) return 0

        val zoneId = ZoneId.systemDefault()
        val workoutDates = timestamps
            .map { DateTimeUtils.millisToLocalDate(it, zoneId) }
            .toSet()

        var streak = 0
        var date = today
        while (workoutDates.contains(date)) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }
}
