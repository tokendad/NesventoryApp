package com.tokendad.nesventorynew.data.repository

import com.tokendad.nesventorynew.data.repository.impl.ItemRepositoryImpl
import com.tokendad.nesventorynew.data.repository.impl.LocationRepositoryImpl
import com.tokendad.nesventorynew.data.repository.impl.MaintenanceRepositoryImpl
import com.tokendad.nesventorynew.data.repository.impl.PrinterRepositoryImpl
import com.tokendad.nesventorynew.data.repository.impl.SystemRepositoryImpl
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
}
