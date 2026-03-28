package com.kb9ut.pror.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kb9ut.pror.data.local.converter.Converters
import com.kb9ut.pror.data.local.dao.BodyWeightDao
import com.kb9ut.pror.data.local.dao.ExerciseDao
import com.kb9ut.pror.data.local.dao.ExerciseGroupDao
import com.kb9ut.pror.data.local.dao.RoutineDao
import com.kb9ut.pror.data.local.dao.SetDao
import com.kb9ut.pror.data.local.dao.WorkoutDao
import com.kb9ut.pror.data.local.dao.WorkoutExerciseDao
import com.kb9ut.pror.data.local.entity.BodyWeightEntity
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.ExerciseGroupEntity
import com.kb9ut.pror.data.local.entity.RoutineEntity
import com.kb9ut.pror.data.local.entity.RoutineExerciseEntity
import com.kb9ut.pror.data.local.entity.RoutineSetTemplateEntity
import com.kb9ut.pror.data.local.entity.WorkoutEntity
import com.kb9ut.pror.data.local.entity.WorkoutExerciseEntity
import com.kb9ut.pror.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        ExerciseGroupEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        RoutineSetTemplateEntity::class,
        BodyWeightEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ProrDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun setDao(): SetDao
    abstract fun exerciseGroupDao(): ExerciseGroupDao
    abstract fun routineDao(): RoutineDao
    abstract fun bodyWeightDao(): BodyWeightDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_sets ADD COLUMN tempo TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE routine_set_templates ADD COLUMN targetTempo TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE routine_exercises ADD COLUMN notes TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workouts_startedAt` ON `workouts` (`startedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workouts_finishedAt` ON `workouts` (`finishedAt`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_sets ADD COLUMN rir INTEGER DEFAULT NULL")
            }
        }
    }
}
