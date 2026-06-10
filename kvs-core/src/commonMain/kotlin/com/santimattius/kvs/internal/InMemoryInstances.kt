package com.santimattius.kvs.internal

import com.santimattius.kvs.internal.memory.InMemoryPreferences
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
private val instances = AtomicReference<Map<String, InMemoryKvs>>(emptyMap())

@OptIn(InternalCoroutinesApi::class)
private val instancesLock = SynchronizedObject()

@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal fun provideInMemoryKvsInstance(name: String): InMemoryKvs {
    instances.load()[name]?.let { return it }
    return synchronized(instancesLock) {
        instances.load()[name]?.let { return it }
        val lock = SynchronizedObject()
        val kvs = InMemoryKvs(InMemoryPreferences(lock), lock)
        instances.store(instances.load() + (name to kvs))
        kvs
    }
}
