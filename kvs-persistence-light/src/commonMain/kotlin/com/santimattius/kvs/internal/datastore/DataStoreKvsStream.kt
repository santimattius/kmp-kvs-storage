package com.santimattius.kvs.internal.datastore

import com.santimattius.kvs.internal.KvsStream
import com.santimattius.kvs.internal.datastore.storage.Storage
import kotlinx.coroutines.flow.Flow

internal class DataStoreKvsStream(private val store: Storage<String>) : KvsStream {

    override fun getAllAsStream(): Flow<Map<String, Any>> = store.getAllAsStream()

    override fun getStringAsStream(key: String, defValue: String): Flow<String> =
        store.readPreferenceAsStream(key, defValue, String::toString)

    override fun getIntAsStream(key: String, defValue: Int): Flow<Int> =
        store.readPreferenceAsStream(key, defValue, String::toIntOrNull)

    override fun getLongAsStream(key: String, defValue: Long): Flow<Long> =
        store.readPreferenceAsStream(key, defValue, String::toLongOrNull)

    override fun getFloatAsStream(key: String, defValue: Float): Flow<Float> =
        store.readPreferenceAsStream(key, defValue, String::toFloatOrNull)

    override fun getBooleanAsStream(key: String, defValue: Boolean): Flow<Boolean> =
        store.readPreferenceAsStream(key, defValue, String::toBoolean)
}
