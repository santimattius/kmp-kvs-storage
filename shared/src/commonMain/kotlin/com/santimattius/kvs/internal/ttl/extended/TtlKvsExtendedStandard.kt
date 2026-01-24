package com.santimattius.kvs.internal.ttl.extended

import androidx.datastore.core.DataStore
import com.santimattius.kvs.internal.datastore.KvsStandard
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import kotlinx.coroutines.flow.first

/**
 * Provides standard (non-Flow) access to TTL-enabled DataStore preferences.
 *
 * This class implements the [KvsStandard] interface with TTL support, offering methods to
 * synchronously retrieve various data types from a [DataStore] while automatically handling
 * expiration. Expired keys are filtered out and cleaned up using lazy cleanup strategy.
 *
 * **Lazy Cleanup Strategy:**
 * - `getAll()`: Cleans up all expired keys in batch before returning results
 * - Individual getters (`getString()`, `getInt()`, etc.): Clean up expired keys when detected
 *
 * @property ds The [DataStore] instance storing [TTLEntity] objects.
 * @property ttlManager The [TtlManager] instance for expiration checks. Defaults to a new instance.
 */
internal class TtlKvsExtendedStandard(
    private val ds: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager = TtlManager()
) : KvsStandard {
    /**
     * Retrieves all non-expired key-value pairs stored.
     *
     * This method performs batch cleanup of expired keys before returning results.
     * Expired keys are automatically removed from storage.
     *
     * @return A map containing all non-expired preferences. The values are of type [Any],
     *         reflecting the type they were stored with (e.g., String, Int, Boolean).
     */
    override suspend fun getAll(): Map<String, Any> {
        val all = ds.data.first()
        val expiredKeys = all.filter { (_, entity) -> isExpired(entity) }.keys
        if (expiredKeys.isNotEmpty()) {
            ds.updateData { data ->
                data.toMutableMap().apply {
                    expiredKeys.forEach { remove(it) }
                }
            }
        }
        return all.filter { (_, entity) -> !isExpired(entity) }.mapValues { it.value.value }
    }

    /**
     * Retrieves a String value from the storage.
     *
     * If the key is expired, it will be automatically removed and [defValue] will be returned.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is expired.
     * @return The preference value if it exists and is not expired, or [defValue].
     */
    override suspend fun getString(key: String, defValue: String): String {
        return getOrDefault(key, defValue, String::toString)
    }

    /**
     * Retrieves an Int value from the storage.
     *
     * If the key is expired, it will be automatically removed and [defValue] will be returned.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is expired.
     * @return The preference value if it exists and is not expired, or [defValue].
     */
    override suspend fun getInt(key: String, defValue: Int): Int {
        return getOrDefault(key, defValue, String::toIntOrNull)
    }

    /**
     * Retrieves a Long value from the storage.
     *
     * If the key is expired, it will be automatically removed and [defValue] will be returned.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is expired.
     * @return The preference value if it exists and is not expired, or [defValue].
     */
    override suspend fun getLong(key: String, defValue: Long): Long {
        return getOrDefault(key, defValue, String::toLongOrNull)
    }

    /**
     * Retrieves a Float value from the storage.
     *
     * If the key is expired, it will be automatically removed and [defValue] will be returned.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is expired.
     * @return The preference value if it exists and is not expired, or [defValue].
     */
    override suspend fun getFloat(key: String, defValue: Float): Float {
        return getOrDefault(key, defValue, String::toFloatOrNull)
    }

    /**
     * Retrieves a Boolean value from the storage.
     *
     * If the key is expired, it will be automatically removed and [defValue] will be returned.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is expired.
     * @return The preference value if it exists and is not expired, or [defValue].
     */
    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean {
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
     * Retrieves a value from storage with lazy cleanup of expired keys.
     *
     * This method implements lazy cleanup: if an expired key is detected during access,
     * it is automatically removed from storage before returning the default value.
     *
     * @param T The type of value to retrieve.
     * @param key The key to retrieve.
     * @param defValue The default value to return if the key doesn't exist or is expired.
     * @param convert A function to convert the String value to type [T].
     * @return The converted value if the key exists and is not expired, or [defValue].
     */
    private suspend fun <T> getOrDefault(key: String, defValue: T, convert: (String) -> T?): T {
        val entity = ds.data.first()[key] ?: return defValue
        if (isExpired(entity)) {
            // Lazy cleanup: remove expired key when detected
            ds.updateData { data ->
                data.toMutableMap().apply {
                    remove(key)
                }
            }
            return defValue
        }
        return convert(entity.value) ?: defValue
    }
}