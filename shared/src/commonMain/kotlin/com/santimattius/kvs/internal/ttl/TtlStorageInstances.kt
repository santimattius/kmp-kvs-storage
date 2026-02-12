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

/**
 * Cache for TTL DataStore instances to ensure singleton behavior per name.
 */
@OptIn(ExperimentalAtomicApi::class)
private val dataStoreInstances =
    AtomicReference<PersistentMap<String, DataStore<Map<String, TTLEntity>>>>(persistentMapOf())

/**
 * Lock object for synchronization when creating new DataStore instances.
 */
@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject()

/**
 * Provides or creates a singleton [DataStore] instance for TTL entities.
 *
 * This function implements a thread-safe singleton pattern using double-check locking.
 * Each unique [name] will correspond to a distinct DataStore instance, ensuring data isolation.
 *
 * @param name The unique name for the DataStore instance. This name is used to generate
 *             the file path and as a cache key.
 * @param encryptor The [Encryptor] instance to use for encryption/decryption.
 * @return A [DataStore] instance associated with the given [name] and [encryptor].
 *         The same instance is returned for subsequent calls with the same [name].
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal fun provideTtlDataStoreInstance(name: String, encryptor: Encryptor): DataStore<Map<String, TTLEntity>> {
    val persistentMap = dataStoreInstances.load()
    persistentMap[name]?.let { return it } // Atomic read

    return synchronized(lock) {
        val persistentMap = dataStoreInstances.load()
        // Double-check locking pattern
        persistentMap[name]?.let { return it }

        val newDataStore = createTllDataStorage("$name.preferences_pb", encryptor)
        dataStoreInstances.store(persistentMap.put(name, newDataStore))
        newDataStore
    }
}