package com.kb9ut.pror.ui.screen.activeworkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.kb9ut.pror.data.local.dao.ExerciseGroupDao
import com.kb9ut.pror.util.FormatUtils
import com.kb9ut.pror.data.local.dao.RoutineDao
import com.kb9ut.pror.data.local.dao.SetDao
import com.kb9ut.pror.data.local.dao.WorkoutExerciseDao
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.ExerciseGroupEntity
import com.kb9ut.pror.data.local.entity.GroupType
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.data.local.entity.WorkoutExerciseEntity
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity
import com.kb9ut.pror.domain.repository.ExerciseRepository
import com.kb9ut.pror.domain.repository.WorkoutRepository
import com.kb9ut.pror.service.MetronomeService
import com.kb9ut.pror.service.MetronomeState
import com.kb9ut.pror.service.RestTimerService
import com.kb9ut.pror.service.RestTimerState
import com.kb9ut.pror.util.OneRepMaxCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PrType { WEIGHT, REPS, ESTIMATED_1RM }

data class PrAchievement(
    val exerciseName: String,
    val type: PrType,
    val value: String
)

data class WorkoutExerciseUiState(
    val workoutExerciseId: Long,
    val exercise: ExerciseEntity,
    val sets: List<WorkoutSetEntity>,
    val previousSets: List<WorkoutSetEntity>,
    val restTimerSeconds: Int = 90,
    val groupId: Long? = null,
    val groupType: GroupType = GroupType.NONE,
    val notes: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val workoutRepository: WorkoutRepository,
    val exerciseRepository: ExerciseRepository,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao,
    private val exerciseGroupDao: ExerciseGroupDao,
    private val routineDao: RoutineDao
) : ViewModel() {

    val workoutId: Long = savedStateHandle.get<Long>("workoutId") ?: 0L

    private val _workoutName = MutableStateFlow("")
    val workoutName: StateFlow<String> = _workoutName.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _workoutFinished = MutableStateFlow(false)
    val workoutFinished: StateFlow<Boolean> = _workoutFinished.asStateFlow()

    private val _workoutDiscarded = MutableStateFlow(false)
    val workoutDiscarded: StateFlow<Boolean> = _workoutDiscarded.asStateFlow()

    private var workoutStartedAt: Long = 0L

    // PR Achievement events
    private val _prAchievementEvent = MutableSharedFlow<PrAchievement>(extraBufferCapacity = 1)
    val prAchievementEvent: SharedFlow<PrAchievement> = _prAchievementEvent.asSharedFlow()

    // Rest Timer
    val restTimerState: StateFlow<RestTimerState> = RestTimerService.timerState

    // Metronome
    private var metronomeService: MetronomeService? = null
    private var metronomeBound = false

    private val _metronomeState = MutableStateFlow(MetronomeState())
    val metronomeState: StateFlow<MetronomeState> = _metronomeState.asStateFlow()

    private val _metronomeActiveExerciseId = MutableStateFlow<Long?>(null)
    val metronomeActiveExerciseId: StateFlow<Long?> = _metronomeActiveExerciseId.asStateFlow()

    private val metronomeConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as MetronomeService.MetronomeBinder).getService()
            metronomeService = service
            metronomeBound = true
            // Forward service state to ViewModel state
            viewModelScope.launch {
                service.state.collect { state ->
                    _metronomeState.value = state
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            metronomeService = null
            metronomeBound = false
        }
    }

    private companion object {
        const val DEFAULT_REST_SECONDS = 90
    }

    // Per-exercise rest timer durations (workoutExerciseId -> seconds)
    private val _exerciseRestTimers = MutableStateFlow<Map<Long, Int>>(emptyMap())

    // Trigger to force reload of previous sets
    private val _previousSetsRefresh = MutableStateFlow(0)

    // Groups for workout
    private val _groupsRefresh = MutableStateFlow(0)

    val exercises: StateFlow<List<WorkoutExerciseUiState>> =
        workoutRepository.getExercisesForWorkout(workoutId)
            .combine(_previousSetsRefresh) { exercises, _ -> exercises }
            .combine(_groupsRefresh) { exercises, _ -> exercises }
            .flatMapLatest { workoutExercises ->
                if (workoutExercises.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val setFlows = workoutExercises.map { we ->
                        workoutRepository.getSetsForWorkoutExercise(we.id)
                            .map { sets -> we to sets }
                    }
                    combine(setFlows) { pairs ->
                        pairs.toList()
                    }
                }
            }
            .combine(_exerciseRestTimers) { pairs, restTimers ->
                val groups = exerciseGroupDao.getGroupsForWorkoutOnce(workoutId)
                val groupMap = groups.associateBy { it.id }

                // Batch load all exercises at once instead of N+1
                val exerciseIds = pairs.map { (we, _) -> we.exerciseId }.distinct()
                val exerciseMap = exerciseRepository.getExercisesByIds(exerciseIds)

                // Batch load all previous sets at once
                val previousSetsMap = loadPreviousSetsBatch(exerciseIds)

                pairs.mapNotNull { (we, sets) ->
                    val exercise = exerciseMap[we.exerciseId] ?: return@mapNotNull null
                    val group = we.groupId?.let { groupMap[it] }
                    WorkoutExerciseUiState(
                        workoutExerciseId = we.id,
                        exercise = exercise,
                        sets = sets,
                        previousSets = previousSetsMap[we.exerciseId] ?: emptyList(),
                        restTimerSeconds = restTimers[we.id] ?: DEFAULT_REST_SECONDS,
                        groupId = we.groupId,
                        groupType = group?.groupType ?: GroupType.NONE,
                        notes = we.notes
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(workoutId)
            if (workout != null) {
                _workoutName.value = workout.name
                workoutStartedAt = workout.startedAt
            }
        }

        // Elapsed timer
        viewModelScope.launch {
            while (true) {
                if (workoutStartedAt > 0L) {
                    _elapsedSeconds.value = ((System.currentTimeMillis() - workoutStartedAt) / 1000).toInt()
                }
                delay(1000)
            }
        }

        // Bind MetronomeService
        val intent = Intent(application, MetronomeService::class.java)
        application.bindService(intent, metronomeConnection, Context.BIND_AUTO_CREATE)
    }

    private suspend fun loadPreviousSets(exerciseId: Long): List<WorkoutSetEntity> {
        val lastWe = workoutExerciseDao.getLastWorkoutExercise(exerciseId, workoutId)
            ?: return emptyList()
        return setDao.getCompletedSetsForWorkoutExerciseOnce(lastWe.id)
    }

    private suspend fun loadPreviousSetsBatch(exerciseIds: List<Long>): Map<Long, List<WorkoutSetEntity>> {
        val result = mutableMapOf<Long, List<WorkoutSetEntity>>()
        for (exerciseId in exerciseIds) {
            result[exerciseId] = loadPreviousSets(exerciseId)
        }
        return result
    }

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch {
            val currentExercises = exercises.value
            val orderIndex = currentExercises.size
            val weId = workoutRepository.addExerciseToWorkout(workoutId, exerciseId, orderIndex)
            // Add one default set
            workoutRepository.addSet(weId, 0)
            _previousSetsRefresh.value++
        }
    }

    fun addSet(workoutExerciseId: Long) {
        viewModelScope.launch {
            val currentSets = exercises.value
                .find { it.workoutExerciseId == workoutExerciseId }
                ?.sets ?: emptyList()
            val orderIndex = currentSets.size
            workoutRepository.addSet(workoutExerciseId, orderIndex)
        }
    }

    fun updateSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            workoutRepository.updateSet(set)
        }
    }

    fun updateSetRir(setId: Long, rir: Int?) {
        viewModelScope.launch {
            setDao.updateRir(setId, rir)
        }
    }

    fun completeSet(setId: Long) {
        viewModelScope.launch {
            val exerciseState = exercises.value.find { state ->
                state.sets.any { it.id == setId }
            } ?: return@launch
            val set = exerciseState.sets.find { it.id == setId } ?: return@launch
            val newCompleted = !set.isCompleted
            workoutRepository.updateSet(
                set.copy(
                    isCompleted = newCompleted,
                    completedAt = if (newCompleted) System.currentTimeMillis() else null
                )
            )
            // Auto-start rest timer when completing a set (not un-completing)
            if (newCompleted && !restTimerState.value.isRunning) {
                val restSeconds = exerciseState.restTimerSeconds
                startRestTimer(restSeconds)
            }
            // Check for PR when completing a set
            if (newCompleted) {
                checkForPr(set, exerciseState.exercise)
            }
        }
    }

    private suspend fun checkForPr(set: WorkoutSetEntity, exercise: ExerciseEntity) {
        val currentWeight = set.weightKg ?: return
        val currentReps = set.reps ?: return
        if (currentWeight <= 0 || currentReps <= 0) return

        val exerciseId = exercise.id
        val exerciseName = exercise.name

        // Check weight PR
        val bestWeightSet = workoutRepository.getPersonalBestByWeight(exerciseId)
        val bestWeight = bestWeightSet?.weightKg ?: 0.0
        if (currentWeight > bestWeight) {
            val weightStr = formatWeight(currentWeight)
            _prAchievementEvent.tryEmit(
                PrAchievement(
                    exerciseName = exerciseName,
                    type = PrType.WEIGHT,
                    value = weightStr
                )
            )
            return // Show the most significant PR only
        }

        // Check reps PR
        val bestRepsSet = workoutRepository.getPersonalBestByReps(exerciseId)
        val bestReps = bestRepsSet?.reps ?: 0
        if (currentReps > bestReps) {
            _prAchievementEvent.tryEmit(
                PrAchievement(
                    exerciseName = exerciseName,
                    type = PrType.REPS,
                    value = "$currentReps reps"
                )
            )
            return
        }

        // Check estimated 1RM PR
        val current1Rm = OneRepMaxCalculator.calculateAll(currentWeight, currentReps)
            ?.median ?: return

        // Get the best historical 1RM from all completed sets for this exercise
        val allSets = workoutRepository.getCompletedSetsForExercise(exerciseId).first()
        val best1Rm = allSets
            .filter { it.id != set.id } // Exclude current set
            .mapNotNull { s ->
                val w = s.weightKg ?: return@mapNotNull null
                val r = s.reps ?: return@mapNotNull null
                if (w > 0 && r > 0) OneRepMaxCalculator.calculateAll(w, r)?.median else null
            }
            .maxOrNull() ?: 0.0

        if (current1Rm > best1Rm) {
            val rmStr = formatWeight(current1Rm)
            _prAchievementEvent.tryEmit(
                PrAchievement(
                    exerciseName = exerciseName,
                    type = PrType.ESTIMATED_1RM,
                    value = rmStr
                )
            )
        }
    }

    private fun formatWeight(weight: Double): String {
        return "${FormatUtils.formatWeightValue(weight)}kg"
    }

    fun updateExerciseNotes(workoutExerciseId: Long, notes: String) {
        viewModelScope.launch {
            val trimmed = notes.ifBlank { null }
            workoutExerciseDao.updateNotes(workoutExerciseId, trimmed)

            // Sync notes back to routine if this workout was started from one
            val workout = workoutRepository.getWorkoutById(workoutId)
            val routineId = workout?.routineId ?: return@launch
            val we = workoutExerciseDao.getById(workoutExerciseId) ?: return@launch
            routineDao.updateRoutineExerciseNotes(routineId, we.exerciseId, trimmed)
        }
    }

    fun setExerciseRestTimer(workoutExerciseId: Long, seconds: Int) {
        _exerciseRestTimers.value = _exerciseRestTimers.value + (workoutExerciseId to seconds)
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteSet(setId)
        }
    }

    fun removeExercise(workoutExerciseId: Long) {
        viewModelScope.launch {
            workoutRepository.removeExerciseFromWorkout(workoutExerciseId)
        }
    }

    fun toggleMetronome(workoutExerciseId: Long, tempo: String?) {
        if (_metronomeActiveExerciseId.value == workoutExerciseId) {
            stopMetronome()
        } else {
            if (tempo != null) {
                metronomeService?.startMetronome(tempo)
                _metronomeActiveExerciseId.value = workoutExerciseId
            }
        }
    }

    fun stopMetronome() {
        metronomeService?.stopMetronome()
        _metronomeActiveExerciseId.value = null
    }

    fun finishWorkout() {
        stopMetronome()
        viewModelScope.launch {
            workoutRepository.finishWorkout(workoutId)
            _workoutFinished.value = true
        }
    }

    fun discardWorkout() {
        stopMetronome()
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            _workoutDiscarded.value = true
        }
    }

    fun updateWorkoutName(name: String) {
        _workoutName.value = name
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(workoutId) ?: return@launch
            workoutRepository.updateWorkout(workout.copy(name = name))
        }
    }

    fun startRestTimer(durationSeconds: Int = DEFAULT_REST_SECONDS) {
        val intent = Intent(application, RestTimerService::class.java).apply {
            action = RestTimerService.ACTION_START
            putExtra(RestTimerService.EXTRA_DURATION_SECONDS, durationSeconds)
        }
        application.startForegroundService(intent)
    }

    fun stopRestTimer() {
        val intent = Intent(application, RestTimerService::class.java).apply {
            action = RestTimerService.ACTION_STOP
        }
        application.startService(intent)
    }

    fun addRestTime(seconds: Int = 30) {
        val intent = Intent(application, RestTimerService::class.java).apply {
            action = RestTimerService.ACTION_ADD_TIME
            putExtra(RestTimerService.EXTRA_ADD_SECONDS, seconds)
        }
        application.startService(intent)
    }

    /**
     * Link two adjacent exercises into a superset.
     * If exerciseA is already in a group, add exerciseB to that group.
     * If exerciseB is already in a group, add exerciseA to that group.
     * Otherwise create a new group.
     */
    fun createSuperset(workoutExerciseIdA: Long, workoutExerciseIdB: Long) {
        viewModelScope.launch {
            val weA = workoutExerciseDao.getById(workoutExerciseIdA) ?: return@launch
            val weB = workoutExerciseDao.getById(workoutExerciseIdB) ?: return@launch

            val groupId: Long
            if (weA.groupId != null) {
                // Add B to A's group
                groupId = weA.groupId
                workoutExerciseDao.update(weB.copy(groupId = groupId))
            } else if (weB.groupId != null) {
                // Add A to B's group
                groupId = weB.groupId
                workoutExerciseDao.update(weA.copy(groupId = groupId))
            } else {
                // Create new group
                val groups = exerciseGroupDao.getGroupsForWorkoutOnce(workoutId)
                val orderIndex = groups.size
                groupId = exerciseGroupDao.insert(
                    ExerciseGroupEntity(
                        workoutId = workoutId,
                        groupType = GroupType.SUPERSET,
                        orderIndex = orderIndex
                    )
                )
                workoutExerciseDao.update(weA.copy(groupId = groupId))
                workoutExerciseDao.update(weB.copy(groupId = groupId))
            }

            // Auto-detect group type based on member count
            updateGroupType(groupId)
            _groupsRefresh.value++
        }
    }

    /**
     * Remove an exercise from its superset group.
     * If the group has only 1 member left after removal, delete the group.
     */
    fun removeFromSuperset(workoutExerciseId: Long) {
        viewModelScope.launch {
            val we = workoutExerciseDao.getById(workoutExerciseId) ?: return@launch
            val groupId = we.groupId ?: return@launch

            workoutExerciseDao.update(we.copy(groupId = null))

            // Check remaining members
            val remaining = exerciseGroupDao.getExerciseCountInGroup(groupId)
            if (remaining <= 1) {
                // Remove the last member from the group and delete the group
                val allExercises = workoutExerciseDao.getExercisesForWorkoutOnce(workoutId)
                allExercises.filter { it.groupId == groupId }.forEach { member ->
                    workoutExerciseDao.update(member.copy(groupId = null))
                }
                exerciseGroupDao.deleteById(groupId)
            } else {
                updateGroupType(groupId)
            }
            _groupsRefresh.value++
        }
    }

    override fun onCleared() {
        stopMetronome()
        if (metronomeBound) {
            application.unbindService(metronomeConnection)
            metronomeBound = false
        }
        super.onCleared()
    }

    /**
     * Update group type based on member count:
     * 2 = SUPERSET, 3 = GIANT_SET (triset), 4+ = CIRCUIT
     */
    private suspend fun updateGroupType(groupId: Long) {
        val group = exerciseGroupDao.getById(groupId) ?: return
        val count = exerciseGroupDao.getExerciseCountInGroup(groupId)
        val newType = when {
            count >= 4 -> GroupType.CIRCUIT
            count == 3 -> GroupType.GIANT_SET
            else -> GroupType.SUPERSET
        }
        if (group.groupType != newType) {
            exerciseGroupDao.insert(group.copy(groupType = newType))
        }
    }
}
