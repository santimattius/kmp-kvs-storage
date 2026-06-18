package com.santimattius.kvs.internal.memory

import com.santimattius.kvs.internal.KvsStandard
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class InMemoryKvsStandard(
    private val preferences: InMemoryPreferences,
    private val mutex: Mutex
) : KvsStandard {

    override suspend fun getAll(): Map<String, Any> = mutex.withLock { preferences.values }

    override suspend fun getString(key: String, defValue: String): String =
        mutex.withLock { preferences.get(key, defValue) as? String ?: defValue }

    override suspend fun getInt(key: String, defValue: Int): Int =
        mutex.withLock { preferences.get(key, defValue) as? Int ?: defValue }

    override suspend fun getLong(key: String, defValue: Long): Long =
        mutex.withLock { preferences.get(key, defValue) as? Long ?: defValue }

    override suspend fun getFloat(key: String, defValue: Float): Float =
        mutex.withLock { preferences.get(key, defValue) as? Float ?: defValue }

    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean =
        mutex.withLock { preferences.get(key, defValue) as? Boolean ?: defValue }
}
