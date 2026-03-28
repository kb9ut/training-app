package com.kb9ut.pror.di

import com.kb9ut.pror.data.repository.BodyWeightRepositoryImpl
import com.kb9ut.pror.data.repository.ExerciseRepositoryImpl
import com.kb9ut.pror.data.repository.RoutineRepositoryImpl
import com.kb9ut.pror.data.repository.WorkoutRepositoryImpl
import com.kb9ut.pror.domain.repository.BodyWeightRepository
import com.kb9ut.pror.domain.repository.ExerciseRepository
import com.kb9ut.pror.domain.repository.RoutineRepository
import com.kb9ut.pror.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(impl: RoutineRepositoryImpl): RoutineRepository

    @Binds
    @Singleton
    abstract fun bindBodyWeightRepository(impl: BodyWeightRepositoryImpl): BodyWeightRepository
}
