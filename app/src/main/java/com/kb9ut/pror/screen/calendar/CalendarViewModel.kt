package com.kb9ut.pror.ui.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.domain.repository.WorkoutRepository
import com.kb9ut.pror.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    val workoutDates: StateFlow<Set<Int>> = _currentYearMonth
        .flatMapLatest { yearMonth ->
            val startOfMonth = DateTimeUtils.startOfDay(yearMonth.atDay(1))
            val endOfMonth = DateTimeUtils.endOfDay(yearMonth.atEndOfMonth())
            workoutRepository.getWorkoutDatesInRange(startOfMonth, endOfMonth)
                .map { timestamps ->
                    timestamps.map { DateTimeUtils.millisToLocalDate(it).dayOfMonth }.toSet()
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val workoutsForSelectedDay: StateFlow<List<WorkoutEntity>> = _selectedDate
        .flatMapLatest { date ->
            if (date != null) {
                val start = DateTimeUtils.startOfDay(date)
                val end = DateTimeUtils.endOfDay(date)
                workoutRepository.getWorkoutsForDay(start, end)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun previousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
        _selectedDate.value = null
    }

    fun nextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
        _selectedDate.value = null
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }
}
