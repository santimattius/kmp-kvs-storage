package com.santimattius.kvs.internal

import com.santimattius.kvs.internal.memory.InMemoryPreferences
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

// Cache for InMemoryKvs instances
@OptIn(ExperimentalAtomicApi::class)
private val inMemoryInstances =
    AtomicReference<PersistentMap<String, InMemoryKvs>>(persistentMapOf())

// Lock object for synchronization
@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject()


@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal fun provideInMemoryKvsInstance(name: String): InMemoryKvs {
    val persistentMap = inMemoryInstances.load()
    persistentMap[name]?.let { return it } // Atomic read

    return synchronized(lock) {
        val persistentMap = inMemoryInstances.load()
        // Double-check locking pattern
        persistentMap[name]?.let { return it }
        val lock = SynchronizedObject()
        val newDataStore = InMemoryKvs(
            preferences = InMemoryPreferences(lock),
            lock = lock
        )
        inMemoryInstances.store(persistentMap.put(name, newDataStore))
        newDataStore
    }
}
