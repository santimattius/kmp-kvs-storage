package com.santimattius.kvs.internal

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
private var appContext: Context? = null

internal fun injectOptimizedContext(context: Context) {
    appContext = context.applicationContext
}

internal fun getOptimizedContext(): Context =
    appContext ?: error("OptimizedKvs: context not initialized. Ensure the app declares OptimizedKvsInitializer via App Startup.")
