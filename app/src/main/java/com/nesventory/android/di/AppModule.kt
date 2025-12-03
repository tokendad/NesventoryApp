package com.nesventory.android.di

import android.content.Context
import com.nesventory.android.data.preferences.PreferencesManager
import com.nesventory.android.data.repository.NesVentoryRepository
import com.nesventory.android.util.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing application-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(
        @ApplicationContext context: Context
    ): NetworkUtils {
        return NetworkUtils(context)
    }

    @Provides
    @Singleton
    fun provideNesVentoryRepository(
        preferencesManager: PreferencesManager,
        networkUtils: NetworkUtils
    ): NesVentoryRepository {
        return NesVentoryRepository(preferencesManager, networkUtils)
    }
}
