package com.tokendad.nesventory.data.repository

import com.tokendad.nesventory.data.network.ConnectivityRepository
import com.tokendad.nesventory.data.network.ConnectivityRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.ItemRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.LocationRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.MaintenanceRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.PrinterRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.SystemRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.UserRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.CollectionRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.GDriveRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.ImportRepositoryImpl
import com.tokendad.nesventory.data.repository.impl.TagRepositoryImpl
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
    abstract fun bindConnectivityRepository(impl: ConnectivityRepositoryImpl): ConnectivityRepository


    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: MaintenanceRepositoryImpl): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindPrinterRepository(impl: PrinterRepositoryImpl): PrinterRepository

    @Binds
    @Singleton
    abstract fun bindSystemRepository(impl: SystemRepositoryImpl): SystemRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindGDriveRepository(impl: GDriveRepositoryImpl): GDriveRepository

    @Binds
    @Singleton
    abstract fun bindImportRepository(impl: ImportRepositoryImpl): ImportRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
