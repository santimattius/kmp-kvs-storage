package com.santimattius.kvs.internal.ttl.extended

import androidx.datastore.core.DataStore
import com.santimattius.kvs.internal.KvsStandard
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import kotlinx.coroutines.flow.first

internal class TtlKvsExtendedStandard(
    private val ds: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager = TtlManager()
) : KvsStandard {

    override suspend fun getAll(): Map<String, Any> {
        val all = ds.data.first()
        val expiredKeys = buildSet {
            for ((k, entity) in all) if (isExpired(entity)) add(k)
        }
        if (expiredKeys.isNotEmpty()) {
            ds.updateData { data ->
                data.toMutableMap().apply { expiredKeys.forEach { remove(it) } }
            }
        }
        return buildMap {
            for ((k, entity) in all) if (!isExpired(entity)) put(k, entity.value)
        }
    }

    override suspend fun getString(key: String, defValue: String): String =
        getOrDefault(key, defValue, String::toString)

    override suspend fun getInt(key: String, defValue: Int): Int =
        getOrDefault(key, defValue, String::toIntOrNull)

    override suspend fun getLong(key: String, defValue: Long): Long =
        getOrDefault(key, defValue, String::toLongOrNull)

    override suspend fun getFloat(key: String, defValue: Float): Float =
        getOrDefault(key, defValue, String::toFloatOrNull)

    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean =
        getOrDefault(key, defValue, String::toBoolean)

    private fun isExpired(entity: TTLEntity): Boolean {
        val expiresAt = entity.expiresAt ?: return false
        return ttlManager.isExpired(expiresAt)
    }

    private suspend fun <T> getOrDefault(key: String, defValue: T, convert: (String) -> T?): T {
        val entity = ds.data.first()[key] ?: return defValue
        if (isExpired(entity)) return defValue
        return convert(entity.value) ?: defValue
    }
}
