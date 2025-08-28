package com.santimattius.kvs.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import okio.Path.Companion.toPath

// Cache for DataStore instances
private val dataStoreInstances =
    atomic<PersistentMap<String, DataStore<Preferences>>>(persistentMapOf())

// Lock object for synchronization
private val lock = SynchronizedObject()

internal fun provideDataStoreInstance(name: String): DataStore<Preferences> {
    dataStoreInstances.value[name]?.let { return it } // Atomic read

    return synchronized(lock) {
        // Double-check locking pattern
        dataStoreInstances.value[name]?.let { return it }

        val newDataStore = getDataStore(name)
        dataStoreInstances.value = dataStoreInstances.value.put(name, newDataStore)
        newDataStore
    }
}

/**
 *   Gets the singleton DataStore instance, creating it if necessary.
 */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal expect fun getDataStore(name: String): DataStore<Preferences>
