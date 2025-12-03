package com.nesventory.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for NesVentory Android app.
 * Uses Hilt for dependency injection.
 */
@HiltAndroidApp
class NesVentoryApplication : Application()
