package com.kb9ut.pror.ui.screen.exercises

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kb9ut.pror.data.local.dao.WorkoutExerciseDao
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity
import com.kb9ut.pror.domain.repository.ExerciseRepository
import com.kb9ut.pror.domain.repository.WorkoutRepository
import com.kb9ut.pror.util.FormatUtils
import com.kb9ut.pror.util.OneRepMaxCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PersonalBests(
    val maxWeight: Double? = null,
    val maxWeightReps: Int? = null,
    val maxReps: Int? = null,
    val maxRepsWeight: Double? = null,
    val estimated1RM: Double? = null
)

data class SetHistoryEntry(
    val set: WorkoutSetEntity,
    val completedAt: Long,
    val workoutExerciseNotes: String? = null
)

sealed class CsvExportResult {
    data object Success : CsvExportResult()
    data class Error(val message: String) : CsvExportResult()
}

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val workoutExerciseDao: WorkoutExerciseDao
) : ViewModel() {

    private val exerciseId: Long = checkNotNull(savedStateHandle["exerciseId"])

    private val _exercise = MutableStateFlow<ExerciseEntity?>(null)
    val exercise: StateFlow<ExerciseEntity?> = _exercise.asStateFlow()

    private val _personalBests = MutableStateFlow(PersonalBests())
    val personalBests: StateFlow<PersonalBests> = _personalBests.asStateFlow()

    val setHistory: StateFlow<List<SetHistoryEntry>> =
        workoutRepository.getCompletedSetsForExercise(exerciseId)
            .map { sets ->
                // Cache workout exercise notes lookups
                val notesCache = mutableMapOf<Long, String?>()
                sets.filter { it.isCompleted && it.completedAt != null && (it.weightKg != null || it.reps != null) }
                    .sortedByDescending { it.completedAt }
                    .map { set ->
                        val notes = notesCache.getOrPut(set.workoutExerciseId) {
                            workoutExerciseDao.getById(set.workoutExerciseId)?.notes
                        }
                        SetHistoryEntry(
                            set = set,
                            completedAt = set.completedAt!!,
                            workoutExerciseNotes = notes
                        )
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadExercise()
        loadPersonalBests()
    }

    private fun loadExercise() {
        viewModelScope.launch {
            _exercise.value = exerciseRepository.getExerciseById(exerciseId)
        }
    }

    private val _csvExportResult = MutableSharedFlow<CsvExportResult>()
    val csvExportResult: SharedFlow<CsvExportResult> = _csvExportResult.asSharedFlow()

    private fun loadPersonalBests() {
        viewModelScope.launch {
            val bestWeight = workoutRepository.getPersonalBestByWeight(exerciseId)
            val bestReps = workoutRepository.getPersonalBestByReps(exerciseId)

            // Observe completed sets for 1RM calculation
            workoutRepository.getCompletedSetsForExercise(exerciseId).collect { sets ->
                val completedSets = sets.filter { it.isCompleted && it.weightKg != null && it.reps != null }
                var maxEstimated1RM: Double? = null
                for (set in completedSets) {
                    val weight = set.weightKg ?: continue
                    val reps = set.reps ?: continue
                    if (reps <= 0 || reps > 30 || weight <= 0) continue
                    val result = OneRepMaxCalculator.calculateAll(weight, reps)
                    if (result != null && (maxEstimated1RM == null || result.average > maxEstimated1RM!!)) {
                        maxEstimated1RM = result.average
                    }
                }

                _personalBests.value = PersonalBests(
                    maxWeight = bestWeight?.weightKg,
                    maxWeightReps = bestWeight?.reps,
                    maxReps = bestReps?.reps,
                    maxRepsWeight = bestReps?.weightKg,
                    estimated1RM = maxEstimated1RM
                )
            }
        }
    }

    fun generateCsvContent(): String {
        val sets = setHistory.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.appendLine("Date,Weight (kg),Reps,RPE,RIR,Set Type,Estimated 1RM")
        for (entry in sets.sortedBy { it.completedAt }) {
            val set = entry.set
            val date = dateFormat.format(Date(entry.completedAt))
            val weight = set.weightKg?.let { FormatUtils.formatWeightValue(it) } ?: ""
            val reps = set.reps?.toString() ?: ""
            val rpe = set.rpe?.let { "%.1f".format(it) } ?: ""
            val rir = set.rir?.toString() ?: ""
            val setType = set.setType.name
            val estimated1rm = if (set.weightKg != null && set.reps != null &&
                set.reps > 0 && set.reps <= 30 && set.weightKg > 0
            ) {
                OneRepMaxCalculator.calculateAll(set.weightKg, set.reps)
                    ?.median?.let { "%.1f".format(it) } ?: ""
            } else {
                ""
            }
            sb.appendLine("$date,$weight,$reps,$rpe,$rir,$setType,$estimated1rm")
        }
        return sb.toString()
    }

    fun exportToCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                val csv = generateCsvContent()
                application.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csv.toByteArray(Charsets.UTF_8))
                } ?: throw Exception("Could not open output stream")
                _csvExportResult.emit(CsvExportResult.Success)
            } catch (e: Exception) {
                _csvExportResult.emit(CsvExportResult.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
