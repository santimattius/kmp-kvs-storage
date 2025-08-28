package com.santimattius.kvs.internal

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.santimattius.kmp.context.getApplicationContext
import kotlin.io.resolve


internal actual fun getDataStore(name: String): DataStore<Preferences> {
    return createDataStore(
        context = getApplicationContext(),
        name = name
    )
}

private fun createDataStore(
    context: Context,
    name: String
): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(name).absolutePath }
)