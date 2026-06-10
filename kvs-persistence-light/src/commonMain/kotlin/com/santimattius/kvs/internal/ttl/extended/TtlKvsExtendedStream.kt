package com.santimattius.kvs.internal.ttl.extended

import androidx.datastore.core.DataStore
import com.santimattius.kvs.internal.KvsStream
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class TtlKvsExtendedStream(
    private val ds: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager = TtlManager()
) : KvsStream {

    override fun getAllAsStream(): Flow<Map<String, Any>> =
        ds.data.map { allValues ->
            buildMap {
                for ((k, entity) in allValues) if (!isExpired(entity)) put(k, entity.value)
            }
        }.distinctUntilChanged()

    override fun getStringAsStream(key: String, defValue: String): Flow<String> =
        getOrDefault(key, defValue, String::toString)

    override fun getIntAsStream(key: String, defValue: Int): Flow<Int> =
        getOrDefault(key, defValue, String::toIntOrNull)

    override fun getLongAsStream(key: String, defValue: Long): Flow<Long> =
        getOrDefault(key, defValue, String::toLongOrNull)

    override fun getFloatAsStream(key: String, defValue: Float): Flow<Float> =
        getOrDefault(key, defValue, String::toFloatOrNull)

    override fun getBooleanAsStream(key: String, defValue: Boolean): Flow<Boolean> =
        getOrDefault(key, defValue, String::toBoolean)

    private fun isExpired(entity: TTLEntity): Boolean {
        val expiresAt = entity.expiresAt ?: return false
        return ttlManager.isExpired(expiresAt)
    }

    private fun <T> getOrDefault(key: String, defValue: T, convert: (String) -> T?): Flow<T> =
        ds.data.map {
            val entity = it[key]
            when {
                entity == null -> defValue
                isExpired(entity) -> defValue
                else -> convert(entity.value) ?: defValue
            }
        }.distinctUntilChanged()
}
