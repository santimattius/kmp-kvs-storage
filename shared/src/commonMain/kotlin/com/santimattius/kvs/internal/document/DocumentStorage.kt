package com.santimattius.kvs.internal.document

import androidx.datastore.core.DataStore
import com.santimattius.kvs.internal.datastore.encrypt.Encryptor
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

// Cache for DataStore instances
@OptIn(ExperimentalAtomicApi::class)
private val dataStoreInstances =
    AtomicReference<PersistentMap<String, DataStore<String>>>(persistentMapOf())

// Lock object for synchronization
@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject()

@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal fun provideDocumentDataStoreInstance(name: String, encryptor: Encryptor): DataStore<String> {
    val persistentMap = dataStoreInstances.load()
    persistentMap[name]?.let { return it } // Atomic read

    return synchronized(lock) {
        val persistentMap = dataStoreInstances.load()
        // Double-check locking pattern
        persistentMap[name]?.let { return it }

        val newDataStore = dataStorage("$name.preferences_pb", encryptor)
        dataStoreInstances.store(persistentMap.put(name, newDataStore))
        newDataStore
    }
}