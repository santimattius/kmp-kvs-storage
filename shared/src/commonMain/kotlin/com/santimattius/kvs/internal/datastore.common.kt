package com.santimattius.kvs.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.*

import okio.Path.Companion.toPath

// Cache for DataStore instances
@OptIn(ExperimentalAtomicApi::class)
private val dataStoreInstances =
    AtomicReference<PersistentMap<String, DataStore<Preferences>>>(persistentMapOf())

// Lock object for synchronization
@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject()

/**
 * Provides a [DataStore] instance for the given name.
 * This function ensures that only one instance of [DataStore] is created for each name.
 * @param name The name of the DataStore.
 * @return A [DataStore] instance.
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal fun provideDataStoreInstance(name: String): DataStore<Preferences> {
    val persistentMap = dataStoreInstances.load()
    persistentMap[name]?.let { return it } // Atomic read

    return synchronized(lock) {
        val persistentMap = dataStoreInstances.load()
        // Double-check locking pattern
        persistentMap[name]?.let { return it }

        val newDataStore = getDataStore("$name.preferences_pb")
        dataStoreInstances.store(persistentMap.put(name, newDataStore))
        newDataStore
    }
}

/**
 * Creates a [DataStore] instance.
 * @param producePath A function that returns the path to the DataStore file.
 * @return A [DataStore] instance.
 */
internal fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

/**
 * Gets a [DataStore] instance for the given name.
 * This is an expected function that must be implemented in the platform-specific code.
 * @param name The name of the DataStore.
 * @return A [DataStore] instance.
 */
internal expect fun getDataStore(name: String): DataStore<Preferences>


/**
 * Produces the path for the DataStore file.
 * This is an expected function that must be implemented in the platform-specific code.
 * @param name The name of the DataStore.
 * @return The path to the DataStore file.
 */
internal expect fun producePath(name: String):String