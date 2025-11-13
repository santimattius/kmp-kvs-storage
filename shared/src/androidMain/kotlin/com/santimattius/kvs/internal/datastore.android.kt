package com.santimattius.kvs.internal

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.santimattius.kmp.context.getApplicationContext
import kotlin.io.resolve

/**
 * Gets the DataStore instance for the given name.
 * This is the Android implementation.
 * @param name The name of the DataStore.
 * @return The DataStore instance.
 */
internal actual fun getDataStore(name: String): DataStore<Preferences> {
    return createDataStore(
        context = getApplicationContext(),
        name = name
    )
}

/**
 * Creates a DataStore instance.
 * @param context The Android context.
 * @param name The name of the DataStore.
 * @return The DataStore instance.
 */
private fun createDataStore(
    context: Context,
    name: String
): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(name).absolutePath }
)

/**
 * Produces the path for the DataStore file.
 * This is the Android implementation.
 * @param name The name of the DataStore.
 * @return The path to the DataStore file.
 */
internal actual fun producePath(name: String): String {
    val applicationContext = getApplicationContext()
    return applicationContext.filesDir.resolve(name).absolutePath
}