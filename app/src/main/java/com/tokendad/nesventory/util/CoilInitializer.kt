package com.tokendad.nesventory.util

import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import okio.Path.Companion.toPath

object CoilInitializer {
    fun initialize(context: Context) {
        SingletonImageLoader.setSafe {
            ImageLoader.Builder(context)
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(context, 0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.cacheDir.resolve("coil_image_cache").absolutePath.toPath())
                        .maxSizePercent(0.02)
                        .build()
                }
                .build()
        }
    }
}
