package com.santimattius.kvs.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import okio.Path.Companion.toPath

@OptIn(ExperimentalAtomicApi::class)
private val dataStoreInstances =
    AtomicReference<PersistentMap<String, DataStore<Preferences>>>(persistentMapOf())

@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject()

@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal fun provideDataStoreInstance(name: String): DataStore<Preferences> {
    val persistentMap = dataStoreInstances.load()
    persistentMap[name]?.let { return it }

    return synchronized(lock) {
        val persistentMap = dataStoreInstances.load()
        persistentMap[name]?.let { return it }

        val newDataStore = getDataStore("$name.preferences_pb")
        dataStoreInstances.store(persistentMap.put(name, newDataStore))
        newDataStore
    }
}

internal fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal expect fun getDataStore(name: String): DataStore<Preferences>

internal expect fun producePath(name: String): String
