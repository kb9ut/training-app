package com.kb9ut.pror.data.backup

import com.kb9ut.pror.data.local.ProrDatabase
import com.kb9ut.pror.data.local.dao.BodyWeightDao
import com.kb9ut.pror.data.local.dao.ExerciseDao
import com.kb9ut.pror.data.local.dao.RoutineDao
import com.kb9ut.pror.data.local.dao.SetDao
import com.kb9ut.pror.data.local.dao.WorkoutDao
import com.kb9ut.pror.data.local.dao.WorkoutExerciseDao
import com.kb9ut.pror.data.local.entity.BodyWeightEntity
import com.kb9ut.pror.data.local.entity.EquipmentCategory
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.MuscleGroup
import com.kb9ut.pror.data.local.entity.RoutineEntity
import com.kb9ut.pror.data.local.entity.RoutineExerciseEntity
import com.kb9ut.pror.data.local.entity.RoutineSetTemplateEntity
import com.kb9ut.pror.data.local.entity.SetType
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.data.local.entity.WorkoutExerciseEntity
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val database: ProrDatabase,
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao,
    private val routineDao: RoutineDao,
    private val bodyWeightDao: BodyWeightDao
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportToJson(): String {
        val exercises = exerciseDao.getAllExercises().first()
        val workouts = workoutDao.getAllWorkouts().first()
        val routines = routineDao.getAllRoutines().first()
        val bodyWeights = bodyWeightDao.getAllBodyWeights().first()

        val exerciseBackups = exercises.map { exercise ->
            ExerciseBackup(
                id = exercise.id,
                name = exercise.name,
                muscleGroup = exercise.muscleGroup.name,
                secondaryMuscleGroup = exercise.secondaryMuscleGroup?.name,
                equipmentCategory = exercise.equipmentCategory.name,
                isCustom = exercise.isCustom,
                notes = exercise.notes
            )
        }

        val workoutBackups = workouts.map { workout ->
            val workoutExercises = workoutExerciseDao.getExercisesForWorkoutOnce(workout.id)
            val exerciseBackupList = workoutExercises.map { we ->
                val sets = setDao.getSetsForWorkoutExerciseOnce(we.id)
                WorkoutExerciseBackup(
                    exerciseId = we.exerciseId,
                    orderIndex = we.orderIndex,
                    notes = we.notes,
                    sets = sets.map { s ->
                        WorkoutSetBackup(
                            orderIndex = s.orderIndex,
                            weightKg = s.weightKg,
                            reps = s.reps,
                            rpe = s.rpe,
                            rir = s.rir,
                            setType = s.setType.name,
                            isCompleted = s.isCompleted,
                            completedAt = s.completedAt,
                            tempo = s.tempo
                        )
                    }
                )
            }
            WorkoutBackup(
                id = workout.id,
                name = workout.name,
                startedAt = workout.startedAt,
                finishedAt = workout.finishedAt,
                durationSeconds = workout.durationSeconds,
                notes = workout.notes,
                exercises = exerciseBackupList
            )
        }

        val routineBackups = routines.map { routine ->
            val routineExercises = routineDao.getRoutineExercises(routine.id)
            val exerciseBackupList = routineExercises.map { re ->
                val setTemplates = routineDao.getRoutineSetTemplates(re.id)
                RoutineExerciseBackup(
                    exerciseId = re.exerciseId,
                    orderIndex = re.orderIndex,
                    notes = re.notes,
                    sets = setTemplates.map { st ->
                        RoutineSetTemplateBackup(
                            orderIndex = st.orderIndex,
                            targetReps = st.targetReps,
                            targetWeightKg = st.targetWeightKg,
                            targetRpe = st.targetRpe,
                            setType = st.setType.name,
                            targetTempo = st.targetTempo
                        )
                    }
                )
            }
            RoutineBackup(
                id = routine.id,
                name = routine.name,
                description = routine.description,
                exercises = exerciseBackupList
            )
        }

        val bodyWeightBackups = bodyWeights.map { bw ->
            BodyWeightBackup(
                weightKg = bw.weightKg,
                recordedAt = bw.recordedAt,
                notes = bw.notes
            )
        }

        val backup = ReppenBackup(
            exercises = exerciseBackups,
            workouts = workoutBackups,
            routines = routineBackups,
            bodyWeights = bodyWeightBackups
        )

        return json.encodeToString(ReppenBackup.serializer(), backup)
    }

    suspend fun importFromJson(jsonString: String) {
        val backup = json.decodeFromString(ReppenBackup.serializer(), jsonString)

        database.runInTransaction {
            // Clear existing data is handled by REPLACE strategy and clearing tables
        }

        // Clear all tables in order (respect foreign keys)
        database.clearAllTables()

        // Import exercises
        for (exercise in backup.exercises) {
            exerciseDao.insert(
                ExerciseEntity(
                    id = exercise.id,
                    name = exercise.name,
                    muscleGroup = MuscleGroup.valueOf(exercise.muscleGroup),
                    secondaryMuscleGroup = exercise.secondaryMuscleGroup?.let { MuscleGroup.valueOf(it) },
                    equipmentCategory = EquipmentCategory.valueOf(exercise.equipmentCategory),
                    isCustom = exercise.isCustom,
                    notes = exercise.notes
                )
            )
        }

        // Import routines
        for (routine in backup.routines) {
            val routineId = routineDao.insertRoutine(
                RoutineEntity(
                    id = routine.id,
                    name = routine.name,
                    description = routine.description
                )
            )
            for (exercise in routine.exercises) {
                val reId = routineDao.insertRoutineExercise(
                    RoutineExerciseEntity(
                        routineId = routineId,
                        exerciseId = exercise.exerciseId,
                        orderIndex = exercise.orderIndex,
                        notes = exercise.notes
                    )
                )
                val templates = exercise.sets.map { st ->
                    RoutineSetTemplateEntity(
                        routineExerciseId = reId,
                        orderIndex = st.orderIndex,
                        targetReps = st.targetReps,
                        targetWeightKg = st.targetWeightKg,
                        targetRpe = st.targetRpe,
                        setType = SetType.valueOf(st.setType),
                        targetTempo = st.targetTempo
                    )
                }
                if (templates.isNotEmpty()) {
                    routineDao.insertRoutineSetTemplates(templates)
                }
            }
        }

        // Import workouts
        for (workout in backup.workouts) {
            val workoutId = workoutDao.insert(
                WorkoutEntity(
                    id = workout.id,
                    name = workout.name,
                    startedAt = workout.startedAt,
                    finishedAt = workout.finishedAt,
                    durationSeconds = workout.durationSeconds,
                    notes = workout.notes
                )
            )
            for (exercise in workout.exercises) {
                val weId = workoutExerciseDao.insert(
                    WorkoutExerciseEntity(
                        workoutId = workoutId,
                        exerciseId = exercise.exerciseId,
                        orderIndex = exercise.orderIndex,
                        notes = exercise.notes
                    )
                )
                val sets = exercise.sets.map { s ->
                    WorkoutSetEntity(
                        workoutExerciseId = weId,
                        orderIndex = s.orderIndex,
                        weightKg = s.weightKg,
                        reps = s.reps,
                        rpe = s.rpe,
                        rir = s.rir,
                        setType = SetType.valueOf(s.setType),
                        isCompleted = s.isCompleted,
                        completedAt = s.completedAt,
                        tempo = s.tempo
                    )
                }
                if (sets.isNotEmpty()) {
                    setDao.insertAll(sets)
                }
            }
        }

        // Import body weights
        for (bw in backup.bodyWeights) {
            bodyWeightDao.insert(
                BodyWeightEntity(
                    weightKg = bw.weightKg,
                    recordedAt = bw.recordedAt,
                    notes = bw.notes
                )
            )
        }
    }
}
