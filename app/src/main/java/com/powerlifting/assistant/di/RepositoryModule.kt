package com.powerlifting.assistant.di

import com.powerlifting.assistant.data.repo.AchievementsRepositoryImpl
import com.powerlifting.assistant.data.repo.NutritionRepositoryImpl
import com.powerlifting.assistant.data.repo.ProfileRepositoryImpl
import com.powerlifting.assistant.data.repo.ProgramRepositoryImpl
import com.powerlifting.assistant.data.repo.WorkoutRepositoryImpl
import com.powerlifting.assistant.domain.repository.AchievementsRepository
import com.powerlifting.assistant.domain.repository.NutritionRepository
import com.powerlifting.assistant.domain.repository.ProfileRepository
import com.powerlifting.assistant.domain.repository.ProgramRepository
import com.powerlifting.assistant.domain.repository.WorkoutRepository
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
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(impl: NutritionRepositoryImpl): NutritionRepository

    @Binds
    @Singleton
    abstract fun bindProgramRepository(impl: ProgramRepositoryImpl): ProgramRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindAchievementsRepository(impl: AchievementsRepositoryImpl): AchievementsRepository
}
