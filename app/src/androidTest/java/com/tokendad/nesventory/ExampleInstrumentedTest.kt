package com.tokendad.nesventory

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // applicationId intentionally remains unchanged for Play Store update continuity.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.tokendad.nesventorynew", appContext.packageName)
    }
}