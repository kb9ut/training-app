package com.kb9ut.pror.data.backup

import com.kb9ut.pror.data.local.dao.ExerciseDao
import com.kb9ut.pror.data.local.dao.SetDao
import com.kb9ut.pror.data.local.dao.WorkoutDao
import com.kb9ut.pror.data.local.dao.WorkoutExerciseDao
import com.kb9ut.pror.data.local.entity.EquipmentCategory
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.MuscleGroup
import com.kb9ut.pror.data.local.entity.SetType
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.data.local.entity.WorkoutExerciseEntity
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.StringReader
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

data class CsvImportResult(
    val workoutsImported: Int,
    val exercisesCreated: Int,
    val setsImported: Int
)

private enum class CsvFormat {
    STRONG, HEVY, FITNOTES
}

@Singleton
class CsvImportManager @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao
) {

    suspend fun importFromCsv(csvContent: String): CsvImportResult = withContext(Dispatchers.IO) {
        val reader = BufferedReader(StringReader(csvContent))
        val headerLine = reader.readLine()
            ?: throw IllegalArgumentException("Empty CSV file")

        val format = detectFormat(headerLine)
        val delimiter = detectDelimiter(headerLine)
        val rows = mutableListOf<List<String>>()

        reader.forEachLine { line ->
            if (line.isNotBlank()) {
                rows.add(parseCsvLine(line, delimiter))
            }
        }

        when (format) {
            CsvFormat.STRONG -> importStrong(headerLine, rows, delimiter)
            CsvFormat.HEVY -> importHevy(headerLine, rows, delimiter)
            CsvFormat.FITNOTES -> importFitNotes(headerLine, rows, delimiter)
        }
    }

    private fun detectFormat(headerLine: String): CsvFormat {
        val header = headerLine.lowercase()
        return when {
            header.contains("workout name") && header.contains("set order") -> CsvFormat.STRONG
            header.contains("start_time") && header.contains("exercise_title") -> CsvFormat.HEVY
            header.contains("category") && header.contains("distance unit") -> CsvFormat.FITNOTES
            else -> throw IllegalArgumentException("UNKNOWN_FORMAT")
        }
    }

    private fun detectDelimiter(headerLine: String): Char {
        // Count semicolons vs commas outside quoted fields to determine delimiter
        var inQuotes = false
        var commaCount = 0
        var semicolonCount = 0
        for (c in headerLine) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> commaCount++
                c == ';' && !inQuotes -> semicolonCount++
            }
        }
        return if (semicolonCount > commaCount) ';' else ','
    }

    // --- Strong format ---

    private suspend fun importStrong(headerLine: String, rows: List<List<String>>, delimiter: Char = ','): CsvImportResult {
        val headers = parseCsvLine(headerLine, delimiter).map { it.lowercase().trim() }
        val dateIdx = headers.indexOf("date")
        val workoutNameIdx = headers.indexOf("workout name")
        val exerciseNameIdx = headers.indexOf("exercise name")
        val setOrderIdx = headers.indexOf("set order")
        val weightIdx = headers.indexOfFirst { it.startsWith("weight") }
        val repsIdx = headers.indexOf("reps")
        val notesIdx = headers.indexOf("notes")
        val workoutNotesIdx = headers.indexOf("workout notes")
        val rpeIdx = headers.indexOf("rpe")
        val rirIdx = headers.indexOf("rir")
        val durationIdx = headers.indexOfFirst { it.startsWith("duration") }

        data class StrongRow(
            val date: String,
            val workoutName: String,
            val exerciseName: String,
            val setOrder: Int,
            val weight: Double?,
            val reps: Int?,
            val notes: String?,
            val workoutNotes: String?,
            val rpe: Double?,
            val rir: Int?,
            val durationSec: Int?
        )

        val parsed = rows.mapNotNull { cols ->
            try {
                // Skip "Note" rows (Set Order = "Note") — they have no weight/reps data
                val setOrderRaw = cols.getOrElse(setOrderIdx) { "1" }
                if (setOrderRaw.equals("Note", ignoreCase = true)) return@mapNotNull null

                StrongRow(
                    date = cols.getOrElse(dateIdx) { "" },
                    workoutName = cols.getOrElse(workoutNameIdx) { "" },
                    exerciseName = cols.getOrElse(exerciseNameIdx) { "" },
                    setOrder = setOrderRaw.toIntOrNull() ?: 1,
                    weight = cols.getOrElse(weightIdx) { "" }.toDoubleOrNull(),
                    reps = cols.getOrElse(repsIdx) { "" }.toIntOrNull(),
                    notes = cols.getOrElse(notesIdx) { "" }.takeIf { it.isNotBlank() },
                    workoutNotes = cols.getOrElse(workoutNotesIdx) { "" }.takeIf { it.isNotBlank() },
                    rpe = cols.getOrElse(rpeIdx) { "" }.toDoubleOrNull(),
                    rir = if (rirIdx >= 0) cols.getOrElse(rirIdx) { "" }.toIntOrNull() else null,
                    durationSec = if (durationIdx >= 0) cols.getOrElse(durationIdx) { "" }.toIntOrNull() else null
                )
            } catch (_: Exception) {
                null
            }
        }

        val workoutGroups = parsed.groupBy { "${it.date}||${it.workoutName}" }

        var exercisesCreated = 0
        var totalSets = 0
        val exerciseCache = mutableMapOf<String, Long>()

        for ((_, groupRows) in workoutGroups) {
            val firstRow = groupRows.first()
            val startedAt = parseStrongDateTime(firstRow.date)
            val durationSeconds = firstRow.durationSec ?: 3600

            val workoutName = firstRow.workoutName.ifBlank {
                "Workout ${firstRow.date.take(10)}"
            }

            val workoutId = workoutDao.insert(
                WorkoutEntity(
                    name = workoutName,
                    startedAt = startedAt,
                    finishedAt = startedAt + (durationSeconds * 1000L),
                    durationSeconds = durationSeconds,
                    notes = firstRow.workoutNotes
                )
            )

            val exerciseGroups = groupRows.groupBy { it.exerciseName }
            var exerciseOrder = 0

            for ((exerciseName, exerciseRows) in exerciseGroups) {
                val exerciseId = getOrCreateExercise(exerciseName, exerciseCache)
                if (exerciseId.second) exercisesCreated++

                val weId = workoutExerciseDao.insert(
                    WorkoutExerciseEntity(
                        workoutId = workoutId,
                        exerciseId = exerciseId.first,
                        orderIndex = exerciseOrder++,
                        notes = exerciseRows.firstOrNull()?.notes
                    )
                )

                val sets = exerciseRows.mapIndexed { idx, row ->
                    WorkoutSetEntity(
                        workoutExerciseId = weId,
                        orderIndex = idx,
                        weightKg = row.weight,
                        reps = row.reps,
                        rpe = row.rpe,
                        rir = row.rir,
                        setType = SetType.NORMAL,
                        isCompleted = true,
                        completedAt = startedAt + (idx * 60_000L)
                    )
                }
                if (sets.isNotEmpty()) {
                    setDao.insertAll(sets)
                    totalSets += sets.size
                }
            }
        }

        return CsvImportResult(
            workoutsImported = workoutGroups.size,
            exercisesCreated = exercisesCreated,
            setsImported = totalSets
        )
    }

    // --- Hevy format ---

    private suspend fun importHevy(headerLine: String, rows: List<List<String>>, delimiter: Char = ','): CsvImportResult {
        val headers = parseCsvLine(headerLine, delimiter).map { it.lowercase().trim() }
        val titleIdx = headers.indexOf("title")
        val startTimeIdx = headers.indexOf("start_time")
        val endTimeIdx = headers.indexOf("end_time")
        val descriptionIdx = headers.indexOf("description")
        val exerciseTitleIdx = headers.indexOf("exercise_title")
        val supersetIdIdx = headers.indexOf("superset_id")
        val exerciseNotesIdx = headers.indexOf("exercise_notes")
        val setIndexIdx = headers.indexOf("set_index")
        val setTypeIdx = headers.indexOf("set_type")
        val weightKgIdx = headers.indexOf("weight_kg")
        val repsIdx = headers.indexOf("reps")
        val rpeIdx = headers.indexOf("rpe")

        data class HevyRow(
            val title: String,
            val startTime: String,
            val endTime: String,
            val description: String?,
            val exerciseTitle: String,
            val supersetId: String?,
            val exerciseNotes: String?,
            val setIndex: Int,
            val setType: String,
            val weightKg: Double?,
            val reps: Int?,
            val rpe: Double?
        )

        val parsed = rows.mapNotNull { cols ->
            try {
                HevyRow(
                    title = cols.getOrElse(titleIdx) { "" },
                    startTime = cols.getOrElse(startTimeIdx) { "" },
                    endTime = cols.getOrElse(endTimeIdx) { "" },
                    description = cols.getOrElse(descriptionIdx) { "" }.takeIf { it.isNotBlank() },
                    exerciseTitle = cols.getOrElse(exerciseTitleIdx) { "" },
                    supersetId = cols.getOrElse(supersetIdIdx) { "" }.takeIf { it.isNotBlank() },
                    exerciseNotes = cols.getOrElse(exerciseNotesIdx) { "" }.takeIf { it.isNotBlank() },
                    setIndex = cols.getOrElse(setIndexIdx) { "0" }.toIntOrNull() ?: 0,
                    setType = cols.getOrElse(setTypeIdx) { "normal" },
                    weightKg = cols.getOrElse(weightKgIdx) { "" }.toDoubleOrNull(),
                    reps = cols.getOrElse(repsIdx) { "" }.toIntOrNull(),
                    rpe = cols.getOrElse(rpeIdx) { "" }.toDoubleOrNull()
                )
            } catch (_: Exception) {
                null
            }
        }

        val workoutGroups = parsed.groupBy { "${it.title}||${it.startTime}" }

        var exercisesCreated = 0
        var totalSets = 0
        val exerciseCache = mutableMapOf<String, Long>()

        for ((_, groupRows) in workoutGroups) {
            val firstRow = groupRows.first()
            val startedAt = parseHevyDateTime(firstRow.startTime)
            val finishedAt = parseHevyDateTime(firstRow.endTime)
            val durationSeconds = if (finishedAt > startedAt) {
                ((finishedAt - startedAt) / 1000).toInt()
            } else {
                null
            }

            val workoutId = workoutDao.insert(
                WorkoutEntity(
                    name = firstRow.title.ifBlank {
                        "Workout ${firstRow.startTime.take(10)}"
                    },
                    startedAt = startedAt,
                    finishedAt = finishedAt,
                    durationSeconds = durationSeconds,
                    notes = firstRow.description
                )
            )

            // Group by exercise title, preserving order of appearance
            val exerciseOrder = mutableListOf<String>()
            val exerciseRowsMap = mutableMapOf<String, MutableList<HevyRow>>()
            for (row in groupRows) {
                if (row.exerciseTitle !in exerciseRowsMap) {
                    exerciseOrder.add(row.exerciseTitle)
                    exerciseRowsMap[row.exerciseTitle] = mutableListOf()
                }
                exerciseRowsMap[row.exerciseTitle]!!.add(row)
            }

            for ((orderIdx, exerciseName) in exerciseOrder.withIndex()) {
                val exerciseRows = exerciseRowsMap[exerciseName]!!
                val exerciseId = getOrCreateExercise(exerciseName, exerciseCache)
                if (exerciseId.second) exercisesCreated++

                val weId = workoutExerciseDao.insert(
                    WorkoutExerciseEntity(
                        workoutId = workoutId,
                        exerciseId = exerciseId.first,
                        orderIndex = orderIdx,
                        notes = exerciseRows.firstOrNull()?.exerciseNotes
                    )
                )

                val sets = exerciseRows.mapIndexed { idx, row ->
                    WorkoutSetEntity(
                        workoutExerciseId = weId,
                        orderIndex = idx,
                        weightKg = row.weightKg,
                        reps = row.reps,
                        rpe = row.rpe,
                        setType = mapHevySetType(row.setType),
                        isCompleted = true,
                        completedAt = startedAt + (idx * 60_000L)
                    )
                }
                if (sets.isNotEmpty()) {
                    setDao.insertAll(sets)
                    totalSets += sets.size
                }
            }
        }

        return CsvImportResult(
            workoutsImported = workoutGroups.size,
            exercisesCreated = exercisesCreated,
            setsImported = totalSets
        )
    }

    // --- FitNotes format ---

    private suspend fun importFitNotes(headerLine: String, rows: List<List<String>>, delimiter: Char = ','): CsvImportResult {
        val headers = parseCsvLine(headerLine, delimiter).map { it.lowercase().trim() }
        val dateIdx = headers.indexOf("date")
        val exerciseIdx = headers.indexOf("exercise")
        val categoryIdx = headers.indexOf("category")
        val weightIdx = headers.indexOfFirst { it.startsWith("weight") }
        val repsIdx = headers.indexOf("reps")

        data class FitNotesRow(
            val date: String,
            val exercise: String,
            val category: String?,
            val weight: Double?,
            val reps: Int?
        )

        val parsed = rows.mapNotNull { cols ->
            try {
                FitNotesRow(
                    date = cols.getOrElse(dateIdx) { "" },
                    exercise = cols.getOrElse(exerciseIdx) { "" },
                    category = cols.getOrElse(categoryIdx) { "" }.takeIf { it.isNotBlank() },
                    weight = cols.getOrElse(weightIdx) { "" }.toDoubleOrNull(),
                    reps = cols.getOrElse(repsIdx) { "" }.toIntOrNull()
                )
            } catch (_: Exception) {
                null
            }
        }

        // Group by date
        val workoutGroups = parsed.groupBy { it.date }

        var exercisesCreated = 0
        var totalSets = 0
        val exerciseCache = mutableMapOf<String, Long>()

        for ((date, groupRows) in workoutGroups) {
            val startedAt = parseFitNotesDate(date)

            val workoutId = workoutDao.insert(
                WorkoutEntity(
                    name = "Workout $date",
                    startedAt = startedAt,
                    finishedAt = startedAt + 3600_000L,
                    durationSeconds = 3600
                )
            )

            // Group by exercise, preserving order
            val exerciseOrder = mutableListOf<String>()
            val exerciseRowsMap = mutableMapOf<String, MutableList<FitNotesRow>>()
            for (row in groupRows) {
                if (row.exercise !in exerciseRowsMap) {
                    exerciseOrder.add(row.exercise)
                    exerciseRowsMap[row.exercise] = mutableListOf()
                }
                exerciseRowsMap[row.exercise]!!.add(row)
            }

            for ((orderIdx, exerciseName) in exerciseOrder.withIndex()) {
                val exerciseRows = exerciseRowsMap[exerciseName]!!
                val exerciseId = getOrCreateExercise(exerciseName, exerciseCache)
                if (exerciseId.second) exercisesCreated++

                val weId = workoutExerciseDao.insert(
                    WorkoutExerciseEntity(
                        workoutId = workoutId,
                        exerciseId = exerciseId.first,
                        orderIndex = orderIdx
                    )
                )

                val sets = exerciseRows.mapIndexed { idx, row ->
                    WorkoutSetEntity(
                        workoutExerciseId = weId,
                        orderIndex = idx,
                        weightKg = row.weight,
                        reps = row.reps,
                        setType = SetType.NORMAL,
                        isCompleted = true,
                        completedAt = startedAt + (idx * 60_000L)
                    )
                }
                if (sets.isNotEmpty()) {
                    setDao.insertAll(sets)
                    totalSets += sets.size
                }
            }
        }

        return CsvImportResult(
            workoutsImported = workoutGroups.size,
            exercisesCreated = exercisesCreated,
            setsImported = totalSets
        )
    }

    // --- Helpers ---

    /**
     * Returns Pair(exerciseId, wasCreated)
     * Tries: original name → Strong mapped English name → Japanese equivalent
     */
    private suspend fun getOrCreateExercise(
        name: String,
        cache: MutableMap<String, Long>
    ): Pair<Long, Boolean> {
        cache[name]?.let { return Pair(it, false) }

        // Try original name
        val existing = exerciseDao.getExerciseByName(name)
        if (existing != null) {
            cache[name] = existing.id
            return Pair(existing.id, false)
        }

        // Try mapped canonical name (English seed name)
        val canonicalEn = STRONG_TO_CANONICAL[name]
        if (canonicalEn != null) {
            val byEn = exerciseDao.getExerciseByName(canonicalEn)
            if (byEn != null) {
                cache[name] = byEn.id
                return Pair(byEn.id, false)
            }
            // Try Japanese equivalent
            val jaName = ENGLISH_TO_JAPANESE[canonicalEn]
            if (jaName != null) {
                val byJa = exerciseDao.getExerciseByName(jaName)
                if (byJa != null) {
                    cache[name] = byJa.id
                    return Pair(byJa.id, false)
                }
            }
        }

        // Determine muscle group and equipment from canonical mapping
        val seedInfo = canonicalEn?.let { CANONICAL_EXERCISE_INFO[it] }
        val newId = exerciseDao.insert(
            ExerciseEntity(
                name = name,
                muscleGroup = seedInfo?.first ?: MuscleGroup.OTHER,
                equipmentCategory = seedInfo?.second ?: EquipmentCategory.OTHER,
                isCustom = true
            )
        )
        cache[name] = newId
        return Pair(newId, true)
    }

    private fun mapHevySetType(type: String): SetType {
        return when (type.lowercase().trim()) {
            "normal" -> SetType.NORMAL
            "warmup", "warm_up" -> SetType.WARMUP
            "dropset", "drop_set" -> SetType.DROP_SET
            "failure" -> SetType.FAILURE
            "amrap" -> SetType.AMRAP
            else -> SetType.NORMAL
        }
    }

    private fun parseStrongDateTime(dateStr: String): Long {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val ldt = LocalDateTime.parse(dateStr, formatter)
            ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
            try {
                // Fallback: date only
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val ld = java.time.LocalDate.parse(dateStr.take(10), formatter)
                ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
                System.currentTimeMillis()
            }
        }
    }

    private fun parseHevyDateTime(dateStr: String): Long {
        return try {
            val odt = OffsetDateTime.parse(dateStr)
            odt.toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val ldt = LocalDateTime.parse(dateStr, formatter)
                ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
                System.currentTimeMillis()
            }
        }
    }

    private fun parseFitNotesDate(dateStr: String): Long {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val ld = java.time.LocalDate.parse(dateStr, formatter)
            ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
            System.currentTimeMillis()
        }
    }

    /**
     * Parse a CSV line handling quoted fields and escaped quotes.
     * Supports both comma and semicolon delimiters.
     */
    private fun parseCsvLine(line: String, delimiter: Char = ','): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> {
                    inQuotes = true
                }
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                c == delimiter && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> {
                    current.append(c)
                }
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    companion object {
        /** Strong CSV exercise name → App canonical English name */
        private val STRONG_TO_CANONICAL = mapOf(
            // Chest
            "Bench Press (Barbell)" to "Barbell Bench Press",
            "Bench Press (Dumbbell)" to "Dumbbell Bench Press",
            "Incline Bench Press (Barbell)" to "Incline Barbell Bench Press",
            "Incline Bench Press (Dumbbell)" to "Incline Dumbbell Bench Press",
            "Incline Dumbbell Press" to "Incline Dumbbell Bench Press",
            "Incline Chest Fly (Dumbbell)" to "Incline Dumbbell Fly",
            "Chest Fly (Dumbbell)" to "Dumbbell Fly",
            "Cable Crossover" to "Cable Fly",
            "Chest Press (Machine)" to "Machine Chest Press",
            "Incline Chest Press (Machine)" to "Machine Chest Press",
            "Iso-Lateral Chest Press (Machine)" to "Machine Chest Press",
            "Chest Dip" to "Dip (Chest)",
            "Chest Dip (Assisted)" to "Dip (Chest)",
            "Bench Press - Close Grip (Barbell)" to "Close Grip Bench Press",
            "Narrow Bench Press" to "Close Grip Bench Press",
            // Back
            "Bent Over Row (Barbell)" to "Barbell Row",
            "Pendlay Row (Barbell)" to "Barbell Row",
            "Bent Over Row (Dumbbell)" to "Dumbbell Row",
            "Bent Over One Arm Row (Dumbbell)" to "Dumbbell Row",
            "Incline Row (Dumbbell)" to "Dumbbell Row",
            "Deadlift (Barbell)" to "Deadlift",
            "Lat Pulldown (Cable)" to "Lat Pulldown",
            "Lat Pulldown - Underhand (Cable)" to "Lat Pulldown",
            "Lat Pulldown (Machine)" to "Lat Pulldown",
            "Lat Pulldown (Single Arm)" to "Lat Pulldown",
            "Seated Row (Cable)" to "Seated Cable Row",
            "Seated Row (Machine)" to "Machine Row",
            "Iso-Lateral Row (Machine)" to "Machine Row",
            "T Bar Row" to "T-Bar Row",
            "Inverted Row (Bodyweight)" to "Pull Up",
            "Wide Pull Up" to "Pull Up",
            "Pull Up (Assisted)" to "Pull Up",
            "Chin Up (Assisted)" to "Chin Up",
            // Shoulders
            "Overhead Press (Barbell)" to "Overhead Press",
            "Seated Overhead Press (Barbell)" to "Overhead Press",
            "Overhead Press (Dumbbell)" to "Dumbbell Shoulder Press",
            "Seated Overhead Press (Dumbbell)" to "Dumbbell Shoulder Press",
            "Lateral Raise (Dumbbell)" to "Lateral Raise",
            "Lateral Raise (Cable)" to "Cable Lateral Raise",
            "Front Raise (Dumbbell)" to "Front Raise",
            "Front Raise (Barbell)" to "Front Raise",
            "Front Raise (Cable)" to "Front Raise",
            "Front Raise (Plate)" to "Front Raise",
            "Face Pull (Cable)" to "Face Pull",
            "Shoulder Press (Machine)" to "Machine Shoulder Press",
            "Shoulder Press (Plate Loaded)" to "Machine Shoulder Press",
            "Rear Raise" to "Rear Delt Fly",
            "Reverse Fly (Cable)" to "Rear Delt Fly",
            "Reverse Fly (Machine)" to "Rear Delt Fly",
            // Biceps
            "Bicep Curl (Barbell)" to "Barbell Curl",
            "Bicep Curl (Dumbbell)" to "Dumbbell Curl",
            "Bicep Curl (Cable)" to "Cable Curl",
            "Bicep Curl (Machine)" to "Cable Curl",
            "Hammer Curl (Dumbbell)" to "Hammer Curl",
            "Preacher Curl (Barbell)" to "Preacher Curl",
            "Preacher Curl (Dumbbell)" to "Preacher Curl",
            "Incline Curl (Dumbbell)" to "Incline Dumbbell Curl",
            "Incline Dumbbell Curls" to "Incline Dumbbell Curl",
            // Triceps
            "Triceps Pushdown (Cable - Straight Bar)" to "Tricep Pushdown",
            "Triceps Extension (Cable)" to "Tricep Pushdown",
            "Triceps Extension (Dumbbell)" to "Overhead Tricep Extension",
            "Triceps Extension (Barbell)" to "Skull Crusher",
            // Legs
            "Squat (Barbell)" to "Barbell Squat",
            "Front Squat (Barbell)" to "Front Squat",
            "Leg Extension (Machine)" to "Leg Extension",
            "Goblet Squat (Kettlebell)" to "Goblet Squat",
            "Lunge (Barbell)" to "Lunge",
            "Seated Leg Press (Machine)" to "Leg Press",
            "Romanian Deadlift (Barbell)" to "Romanian Deadlift",
            "Romanian Deadlift (Dumbbell)" to "Dumbbell Romanian Deadlift",
            "Stiff Leg Deadlift (Barbell)" to "Stiff Leg Deadlift",
            "Seated Leg Curl (Machine)" to "Seated Leg Curl",
            "Leg Curls" to "Lying Leg Curl",
            // Calves
            "Calf Raise" to "Standing Calf Raise",
            // Abs
            "Ab Wheel" to "Ab Wheel Rollout",
            // Traps
            "Shrug (Dumbbell)" to "Dumbbell Shrug",
            "Shrug (Smith Machine)" to "Barbell Shrug",
            // Forearms
            "Reverse Curl (Barbell)" to "Reverse Wrist Curl",
        )

        /** App English seed name → Japanese seed name */
        private val ENGLISH_TO_JAPANESE = mapOf(
            // Chest
            "Barbell Bench Press" to "バーベルベンチプレス",
            "Incline Barbell Bench Press" to "インクラインバーベルベンチプレス",
            "Decline Barbell Bench Press" to "デクラインバーベルベンチプレス",
            "Dumbbell Bench Press" to "ダンベルベンチプレス",
            "Incline Dumbbell Bench Press" to "インクラインダンベルベンチプレス",
            "Dumbbell Fly" to "ダンベルフライ",
            "Incline Dumbbell Fly" to "インクラインダンベルフライ",
            "Cable Fly" to "ケーブルフライ",
            "Machine Chest Press" to "チェストプレスマシン",
            "Pec Deck" to "ペックデック",
            "Push Up" to "プッシュアップ",
            "Dip (Chest)" to "ディップス(胸)",
            // Back
            "Barbell Row" to "バーベルロウ",
            "Deadlift" to "デッドリフト",
            "Dumbbell Row" to "ダンベルロウ",
            "Pull Up" to "懸垂",
            "Chin Up" to "チンアップ",
            "Lat Pulldown" to "ラットプルダウン",
            "Seated Cable Row" to "シーテッドケーブルロウ",
            "T-Bar Row" to "Tバーロウ",
            "Machine Row" to "マシンロウ",
            // Shoulders
            "Overhead Press" to "オーバーヘッドプレス",
            "Dumbbell Shoulder Press" to "ダンベルショルダープレス",
            "Lateral Raise" to "サイドレイズ",
            "Front Raise" to "フロントレイズ",
            "Rear Delt Fly" to "リアデルトフライ",
            "Face Pull" to "フェイスプル",
            "Machine Shoulder Press" to "ショルダープレスマシン",
            "Cable Lateral Raise" to "ケーブルサイドレイズ",
            // Biceps
            "Barbell Curl" to "バーベルカール",
            "Dumbbell Curl" to "ダンベルカール",
            "Hammer Curl" to "ハンマーカール",
            "Preacher Curl" to "プリーチャーカール",
            "Incline Dumbbell Curl" to "インクラインダンベルカール",
            "Cable Curl" to "ケーブルカール",
            "Concentration Curl" to "コンセントレーションカール",
            // Triceps
            "Tricep Pushdown" to "トライセプスプッシュダウン",
            "Overhead Tricep Extension" to "オーバーヘッドトライセプスエクステンション",
            "Skull Crusher" to "スカルクラッシャー",
            "Close Grip Bench Press" to "ナローベンチプレス",
            "Dip (Tricep)" to "ディップス(三頭筋)",
            "Tricep Kickback" to "トライセプスキックバック",
            // Quadriceps
            "Barbell Squat" to "バーベルスクワット",
            "Front Squat" to "フロントスクワット",
            "Leg Press" to "レッグプレス",
            "Leg Extension" to "レッグエクステンション",
            "Hack Squat" to "ハックスクワット",
            "Bulgarian Split Squat" to "ブルガリアンスプリットスクワット",
            "Goblet Squat" to "ゴブレットスクワット",
            "Lunge" to "ランジ",
            // Hamstrings
            "Romanian Deadlift" to "ルーマニアンデッドリフト",
            "Lying Leg Curl" to "ライイングレッグカール",
            "Seated Leg Curl" to "シーテッドレッグカール",
            "Stiff Leg Deadlift" to "スティッフレッグデッドリフト",
            "Dumbbell Romanian Deadlift" to "ダンベルルーマニアンデッドリフト",
            "Nordic Hamstring Curl" to "ノルディックハムストリングカール",
            // Glutes
            "Hip Thrust" to "ヒップスラスト",
            "Glute Bridge" to "グルートブリッジ",
            "Cable Pull Through" to "ケーブルプルスルー",
            "Glute Kickback (Machine)" to "グルートキックバック(マシン)",
            // Calves
            "Standing Calf Raise" to "スタンディングカーフレイズ",
            "Seated Calf Raise" to "シーテッドカーフレイズ",
            "Leg Press Calf Raise" to "レッグプレスカーフレイズ",
            // Abs
            "Crunch" to "クランチ",
            "Cable Crunch" to "ケーブルクランチ",
            "Hanging Leg Raise" to "ハンギングレッグレイズ",
            "Ab Wheel Rollout" to "アブローラー",
            "Plank" to "プランク",
            // Traps
            "Barbell Shrug" to "バーベルシュラッグ",
            "Dumbbell Shrug" to "ダンベルシュラッグ",
            // Forearms
            "Wrist Curl" to "リストカール",
            "Reverse Wrist Curl" to "リバースリストカール",
        )

        /** Canonical English name → (MuscleGroup, EquipmentCategory) for new exercises */
        private val CANONICAL_EXERCISE_INFO: Map<String, Pair<MuscleGroup, EquipmentCategory>> = mapOf(
            "Barbell Bench Press" to (MuscleGroup.CHEST to EquipmentCategory.BARBELL),
            "Incline Barbell Bench Press" to (MuscleGroup.CHEST to EquipmentCategory.BARBELL),
            "Dumbbell Bench Press" to (MuscleGroup.CHEST to EquipmentCategory.DUMBBELL),
            "Incline Dumbbell Bench Press" to (MuscleGroup.CHEST to EquipmentCategory.DUMBBELL),
            "Dumbbell Fly" to (MuscleGroup.CHEST to EquipmentCategory.DUMBBELL),
            "Incline Dumbbell Fly" to (MuscleGroup.CHEST to EquipmentCategory.DUMBBELL),
            "Cable Fly" to (MuscleGroup.CHEST to EquipmentCategory.CABLE),
            "Machine Chest Press" to (MuscleGroup.CHEST to EquipmentCategory.MACHINE),
            "Dip (Chest)" to (MuscleGroup.CHEST to EquipmentCategory.BODYWEIGHT),
            "Close Grip Bench Press" to (MuscleGroup.TRICEPS to EquipmentCategory.BARBELL),
            "Barbell Row" to (MuscleGroup.BACK to EquipmentCategory.BARBELL),
            "Dumbbell Row" to (MuscleGroup.BACK to EquipmentCategory.DUMBBELL),
            "Deadlift" to (MuscleGroup.BACK to EquipmentCategory.BARBELL),
            "Lat Pulldown" to (MuscleGroup.LATS to EquipmentCategory.CABLE),
            "Seated Cable Row" to (MuscleGroup.BACK to EquipmentCategory.CABLE),
            "T-Bar Row" to (MuscleGroup.BACK to EquipmentCategory.BARBELL),
            "Machine Row" to (MuscleGroup.BACK to EquipmentCategory.MACHINE),
            "Pull Up" to (MuscleGroup.BACK to EquipmentCategory.BODYWEIGHT),
            "Chin Up" to (MuscleGroup.BACK to EquipmentCategory.BODYWEIGHT),
            "Overhead Press" to (MuscleGroup.SHOULDERS to EquipmentCategory.BARBELL),
            "Dumbbell Shoulder Press" to (MuscleGroup.SHOULDERS to EquipmentCategory.DUMBBELL),
            "Lateral Raise" to (MuscleGroup.SHOULDERS to EquipmentCategory.DUMBBELL),
            "Cable Lateral Raise" to (MuscleGroup.SHOULDERS to EquipmentCategory.CABLE),
            "Front Raise" to (MuscleGroup.SHOULDERS to EquipmentCategory.DUMBBELL),
            "Face Pull" to (MuscleGroup.SHOULDERS to EquipmentCategory.CABLE),
            "Machine Shoulder Press" to (MuscleGroup.SHOULDERS to EquipmentCategory.MACHINE),
            "Rear Delt Fly" to (MuscleGroup.SHOULDERS to EquipmentCategory.DUMBBELL),
            "Barbell Curl" to (MuscleGroup.BICEPS to EquipmentCategory.BARBELL),
            "Dumbbell Curl" to (MuscleGroup.BICEPS to EquipmentCategory.DUMBBELL),
            "Cable Curl" to (MuscleGroup.BICEPS to EquipmentCategory.CABLE),
            "Hammer Curl" to (MuscleGroup.BICEPS to EquipmentCategory.DUMBBELL),
            "Preacher Curl" to (MuscleGroup.BICEPS to EquipmentCategory.BARBELL),
            "Incline Dumbbell Curl" to (MuscleGroup.BICEPS to EquipmentCategory.DUMBBELL),
            "Tricep Pushdown" to (MuscleGroup.TRICEPS to EquipmentCategory.CABLE),
            "Overhead Tricep Extension" to (MuscleGroup.TRICEPS to EquipmentCategory.DUMBBELL),
            "Skull Crusher" to (MuscleGroup.TRICEPS to EquipmentCategory.BARBELL),
            "Barbell Squat" to (MuscleGroup.QUADRICEPS to EquipmentCategory.BARBELL),
            "Front Squat" to (MuscleGroup.QUADRICEPS to EquipmentCategory.BARBELL),
            "Leg Press" to (MuscleGroup.QUADRICEPS to EquipmentCategory.MACHINE),
            "Leg Extension" to (MuscleGroup.QUADRICEPS to EquipmentCategory.MACHINE),
            "Hack Squat" to (MuscleGroup.QUADRICEPS to EquipmentCategory.MACHINE),
            "Bulgarian Split Squat" to (MuscleGroup.QUADRICEPS to EquipmentCategory.DUMBBELL),
            "Goblet Squat" to (MuscleGroup.QUADRICEPS to EquipmentCategory.DUMBBELL),
            "Lunge" to (MuscleGroup.QUADRICEPS to EquipmentCategory.DUMBBELL),
            "Romanian Deadlift" to (MuscleGroup.HAMSTRINGS to EquipmentCategory.BARBELL),
            "Dumbbell Romanian Deadlift" to (MuscleGroup.HAMSTRINGS to EquipmentCategory.DUMBBELL),
            "Stiff Leg Deadlift" to (MuscleGroup.HAMSTRINGS to EquipmentCategory.BARBELL),
            "Lying Leg Curl" to (MuscleGroup.HAMSTRINGS to EquipmentCategory.MACHINE),
            "Seated Leg Curl" to (MuscleGroup.HAMSTRINGS to EquipmentCategory.MACHINE),
            "Standing Calf Raise" to (MuscleGroup.CALVES to EquipmentCategory.MACHINE),
            "Ab Wheel Rollout" to (MuscleGroup.ABS to EquipmentCategory.OTHER),
            "Dumbbell Shrug" to (MuscleGroup.TRAPS to EquipmentCategory.DUMBBELL),
            "Barbell Shrug" to (MuscleGroup.TRAPS to EquipmentCategory.BARBELL),
            "Reverse Wrist Curl" to (MuscleGroup.FOREARMS to EquipmentCategory.BARBELL),
        )
    }
}
