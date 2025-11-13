package com.santimattius.kvs.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * Gets the DataStore instance for the given name.
 * This is the iOS implementation.
 * @param name The name of the DataStore.
 * @return The DataStore instance.
 */
internal actual fun getDataStore(name: String): DataStore<Preferences> {
    return createDataStore(name)
}

/**
 * Creates a DataStore instance.
 * @param name The name of the DataStore.
 * @return The DataStore instance.
 */
@OptIn(ExperimentalForeignApi::class)
private fun createDataStore(name: String): DataStore<Preferences> = createDataStore(
    producePath = { producePath(name) },
)

/**
 * Produces the path for the DataStore file.
 * This is the iOS implementation.
 * @param name The name of the DataStore.
 * @return The path to the DataStore file.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun producePath(name: String): String {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory).path + "/$name"
}