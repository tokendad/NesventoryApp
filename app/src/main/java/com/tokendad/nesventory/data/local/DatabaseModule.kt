package com.tokendad.nesventory.data.local

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NesVentoryDatabase {
        return Room.databaseBuilder(
            context,
            NesVentoryDatabase::class.java,
            "nesventory.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideItemDao(database: NesVentoryDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideLocationDao(database: NesVentoryDatabase): LocationDao = database.locationDao()

    @Provides
    fun provideSyncQueueDao(database: NesVentoryDatabase): SyncQueueDao = database.syncQueueDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
