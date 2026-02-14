package com.santimattius.kvs.internal.ttl.extended

import androidx.datastore.core.DataStore
import com.santimattius.kvs.internal.datastore.KvsStream
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Provides Flow-based access to TTL-enabled DataStore preferences.
 *
 * This class implements the [KvsStream] interface with TTL support, offering methods to observe
 * changes to various data types stored in a [DataStore] as a [Flow]. Expired keys are automatically
 * filtered out from the stream emissions.
 *
 * **GC-friendly:** Uses [distinctUntilChanged] so the same filtered map is not re-emitted,
 * reducing allocations when DataStore emits repeatedly with unchanged content.
 * Cleanup is handled by [TtlKvsExtendedStandard.getAll] or the background [com.santimattius.kvs.internal.ttl.cleanup.CleanupJob].
 *
 * @property ds The [DataStore] instance storing [TTLEntity] objects.
 * @property ttlManager The [TtlManager] instance for expiration checks. Defaults to a new instance.
 */
internal class TtlKvsExtendedStream(
    private val ds: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager = TtlManager()
) : KvsStream {

    /**
     * Returns a [Flow] that emits a map of all non-expired key-value pairs upon any change.
     *
     * Expired keys are automatically filtered out from the emitted map.
     *
     * @return A [Flow] emitting a map of all non-expired preferences. Keys are preference names,
     *         and values are of type [Any], reflecting their original stored type.
     */
    override fun getAllAsStream(): Flow<Map<String, Any>> {
        return ds.data
            .map { allValues ->
                buildMap {
                    for ((k, entity) in allValues) if (!isExpired(entity)) put(k, entity.value)
                }
            }
            .distinctUntilChanged()
    }

    /**
     * Returns a [Flow] that emits updates to a String preference value.
     *
     * If the key is expired, the flow will emit [defValue].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is expired.
     * @return A [Flow] emitting the String preference value, or [defValue] if expired or missing.
     */
    override fun getStringAsStream(
        key: String,
        defValue: String
    ): Flow<String> {
        return getOrDefault(key, defValue, String::toString)
    }

    /**
     * Returns a [Flow] that emits updates to an Int preference value.
     *
     * If the key is expired, the flow will emit [defValue].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is expired.
     * @return A [Flow] emitting the Int preference value, or [defValue] if expired or missing.
     */
    override fun getIntAsStream(
        key: String,
        defValue: Int
    ): Flow<Int> {
        return getOrDefault(key, defValue, String::toIntOrNull)
    }

    /**
     * Returns a [Flow] that emits updates to a Long preference value.
     *
     * If the key is expired, the flow will emit [defValue].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is expired.
     * @return A [Flow] emitting the Long preference value, or [defValue] if expired or missing.
     */
    override fun getLongAsStream(
        key: String,
        defValue: Long
    ): Flow<Long> {
        return getOrDefault(key, defValue, String::toLongOrNull)
    }

    /**
     * Returns a [Flow] that emits updates to a Float preference value.
     *
     * If the key is expired, the flow will emit [defValue].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is expired.
     * @return A [Flow] emitting the Float preference value, or [defValue] if expired or missing.
     */
    override fun getFloatAsStream(
        key: String,
        defValue: Float
    ): Flow<Float> {
        return getOrDefault(key, defValue, String::toFloatOrNull)
    }

    /**
     * Returns a [Flow] that emits updates to a Boolean preference value.
     *
     * If the key is expired, the flow will emit [defValue].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is expired.
     * @return A [Flow] emitting the Boolean preference value, or [defValue] if expired or missing.
     */
    override fun getBooleanAsStream(
        key: String,
        defValue: Boolean
    ): Flow<Boolean> {
        return getOrDefault(key, defValue, String::toBoolean)
    }

    /**
     * Checks if an entity has expired based on its expiration timestamp.
     *
     * @param entity The [TTLEntity] to check.
     * @return `true` if the entity has expired, `false` otherwise.
     *         If [entity.expiresAt] is `null`, returns `false` (entity never expires).
     */
    private fun isExpired(entity: TTLEntity): Boolean {
        val expiresAt = entity.expiresAt ?: return false
        return ttlManager.isExpired(expiresAt)
    }

    /**
     * Creates a [Flow] that emits the value for a key, or [defValue] if the key doesn't exist or is expired.
     *
     * @param T The type of value to retrieve.
     * @param key The key to observe.
     * @param defValue The default value to emit if the key doesn't exist or is expired.
     * @param convert A function to convert the String value to type [T].
     * @return A [Flow] emitting the converted value or [defValue].
     */
    private fun <T> getOrDefault(key: String, defValue: T, convert: (String) -> T?): Flow<T> {
        return ds.data
            .map {
                val entity = it[key]
                when {
                    entity == null -> defValue
                    isExpired(entity) -> defValue
                    else -> convert(entity.value) ?: defValue
                }
            }
            .distinctUntilChanged()
    }
}