package com.santimattius.kvs.internal.memory

import com.santimattius.kvs.internal.KvsStandard
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal class InMemoryKvsStandard(
    private val preferences: InMemoryPreferences,
    private val lock: SynchronizedObject
) : KvsStandard {

    override suspend fun getAll(): Map<String, Any> = synchronized(lock) { preferences.values }

    override suspend fun getString(key: String, defValue: String): String =
        synchronized(lock) { preferences.get(key, defValue) as? String ?: defValue }

    override suspend fun getInt(key: String, defValue: Int): Int =
        synchronized(lock) { preferences.get(key, defValue) as? Int ?: defValue }

    override suspend fun getLong(key: String, defValue: Long): Long =
        synchronized(lock) { preferences.get(key, defValue) as? Long ?: defValue }

    override suspend fun getFloat(key: String, defValue: Float): Float =
        synchronized(lock) { preferences.get(key, defValue) as? Float ?: defValue }

    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean =
        synchronized(lock) { preferences.get(key, defValue) as? Boolean ?: defValue }
}
