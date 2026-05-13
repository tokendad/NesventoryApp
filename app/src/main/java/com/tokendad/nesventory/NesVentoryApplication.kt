package com.tokendad.nesventory

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Constraints
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tokendad.nesventory.data.network.ConnectivityRepository
import com.tokendad.nesventory.data.preferences.MigrationManager
import com.tokendad.nesventory.data.sync.SyncWorker
import com.tokendad.nesventory.util.CoilInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class NesVentoryApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var migrationManager: MigrationManager
    @Inject lateinit var connectivityRepository: ConnectivityRepository
    @Inject lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        CoilInitializer.initialize(this)
        applicationScope.launch {
            migrationManager.runMigrations()
        }
        schedulePeriodicSync()
        observeConnectivityForSync()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val periodic = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic
        )
    }

    private fun observeConnectivityForSync() {
        applicationScope.launch {
            connectivityRepository.isConnected.collectLatest { connected ->
                if (connected) {
                    val work = OneTimeWorkRequestBuilder<SyncWorker>()
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                    WorkManager.getInstance(this@NesVentoryApplication).enqueueUniqueWork(
                        RECONNECT_SYNC_WORK,
                        ExistingWorkPolicy.REPLACE,
                        work
                    )
                }
            }
        }
    }

    companion object {
        private const val PERIODIC_SYNC_WORK = "nesventory_periodic_sync"
        private const val RECONNECT_SYNC_WORK = "nesventory_reconnect_sync"
    }
}