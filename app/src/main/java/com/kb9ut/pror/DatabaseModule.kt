package com.kb9ut.pror.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kb9ut.pror.data.local.ProrDatabase
import com.kb9ut.pror.data.local.dao.BodyWeightDao
import com.kb9ut.pror.data.local.dao.ExerciseDao
import com.kb9ut.pror.data.local.dao.ExerciseGroupDao
import com.kb9ut.pror.data.local.dao.RoutineDao
import com.kb9ut.pror.data.local.dao.SetDao
import com.kb9ut.pror.data.local.dao.WorkoutDao
import com.kb9ut.pror.data.local.dao.WorkoutExerciseDao
import com.kb9ut.pror.data.local.seed.ExerciseSeedData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ProrDatabase {
        return Room.databaseBuilder(
            context,
            ProrDatabase::class.java,
            "ironlog.db"
        )
            .addMigrations(ProrDatabase.MIGRATION_1_2, ProrDatabase.MIGRATION_2_3, ProrDatabase.MIGRATION_3_4, ProrDatabase.MIGRATION_4_5)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        val database = Room.databaseBuilder(
                            context,
                            ProrDatabase::class.java,
                            "ironlog.db"
                        ).build()
                        val dao = database.exerciseDao()
                        if (dao.getCount() == 0) {
                            dao.insertAll(ExerciseSeedData.getAll())
                        }
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideExerciseDao(db: ProrDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideWorkoutDao(db: ProrDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideWorkoutExerciseDao(db: ProrDatabase): WorkoutExerciseDao = db.workoutExerciseDao()

    @Provides
    fun provideSetDao(db: ProrDatabase): SetDao = db.setDao()

    @Provides
    fun provideRoutineDao(db: ProrDatabase): RoutineDao = db.routineDao()

    @Provides
    fun provideExerciseGroupDao(db: ProrDatabase): ExerciseGroupDao = db.exerciseGroupDao()

    @Provides
    fun provideBodyWeightDao(db: ProrDatabase): BodyWeightDao = db.bodyWeightDao()
}
