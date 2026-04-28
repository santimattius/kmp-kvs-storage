package com.santimattius.kvs.internal.context

import android.content.Context
import androidx.startup.Initializer
import com.santimattius.kmp.context.injectContext

class AppContextInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        injectContext(context = context)
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()
}
