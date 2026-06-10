@file:OptIn(com.santimattius.kvs.ExperimentalKvsTtl::class)
package com.santimattius.kvs.internal.ttl

import androidx.datastore.core.DataStore
import com.santimattius.kvs.internal.datastore.encrypt.Encryptor
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
private val dataStoreInstances =
    AtomicReference<PersistentMap<String, DataStore<Map<String, TTLEntity>>>>(persistentMapOf())

@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject()

@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal fun provideTtlDataStoreInstance(name: String, encryptor: Encryptor): DataStore<Map<String, TTLEntity>> {
    val persistentMap = dataStoreInstances.load()
    persistentMap[name]?.let { return it }

    return synchronized(lock) {
        val persistentMap = dataStoreInstances.load()
        persistentMap[name]?.let { return it }

        val newDataStore = createTllDataStorage("$name.preferences_pb", encryptor)
        dataStoreInstances.store(persistentMap.put(name, newDataStore))
        newDataStore
    }
}
