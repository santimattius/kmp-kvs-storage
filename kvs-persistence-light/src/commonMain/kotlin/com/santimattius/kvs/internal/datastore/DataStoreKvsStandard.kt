package com.santimattius.kvs.internal.datastore

import com.santimattius.kvs.internal.KvsStandard
import com.santimattius.kvs.internal.datastore.storage.Storage

internal class DataStoreKvsStandard(private val store: Storage<String>) : KvsStandard {

    override suspend fun getAll(): Map<String, Any> = store.getAll()

    override suspend fun getString(key: String, defValue: String): String =
        store.readPreference(key, defValue, String::toString)

    override suspend fun getInt(key: String, defValue: Int): Int =
        store.readPreference(key, defValue, String::toIntOrNull)

    override suspend fun getLong(key: String, defValue: Long): Long =
        store.readPreference(key, defValue, String::toLongOrNull)

    override suspend fun getFloat(key: String, defValue: Float): Float =
        store.readPreference(key, defValue, String::toFloatOrNull)

    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean =
        store.readPreference(key, defValue, String::toBoolean)
}
