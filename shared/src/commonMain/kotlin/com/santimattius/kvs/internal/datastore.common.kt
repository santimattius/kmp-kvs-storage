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
 *   Gets the singleton DataStore instance, creating it if necessary.
 */
internal fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal expect fun getDataStore(name: String): DataStore<Preferences>
