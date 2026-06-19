package com.santimattius.kvs.android

import android.content.Context
import androidx.startup.Initializer

class OptimizedKvsInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        injectOptimizedContext(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = emptyList()
}
