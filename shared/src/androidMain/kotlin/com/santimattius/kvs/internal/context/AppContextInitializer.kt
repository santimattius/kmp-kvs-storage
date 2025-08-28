package com.santimattius.kvs.internal.context

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.santimattius.kmp.context.injectContext

class AppContextInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        injectContext(context = context)
        Log.d("AppContextInitializer", "AppContextInitializer initialized")
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}