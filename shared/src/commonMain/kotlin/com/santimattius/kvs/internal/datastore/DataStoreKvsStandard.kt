package com.santimattius.kvs.internal.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.santimattius.kvs.internal.datastore.storage.Storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first

/**
 * Provides standard (non-Flow) access to DataStore preferences.
 *
 * This class implements the [KvsStandard] interface, offering methods to synchronously
 * retrieve various data types from a [Storage]. It's designed for use cases where
 * a single, current value is needed rather than a stream of updates.
 *
 * @property store The [Storage] instance used for data persistence.
 */
internal class DataStoreKvsStandard(
    private val store: Storage<String>,
) : KvsStandard {

    /**
     * Retrieves all key-value pairs stored in the DataStore.
     *
     * @return A map containing all stored preferences. The keys are the preference names,
     *         and the values are of type [Any], reflecting their original stored type.
     */
    override suspend fun getAll(): Map<String, Any> {
        return store.getAll()
    }

    /**
     * Retrieves a String value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    override suspend fun getString(key: String, defValue: String): String {
        return store.readPreference(
            key = key,
            defaultValue = defValue,
            converter = { it }
        )
    }

    /**
     * Retrieves an Int value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or if the stored value is not a valid Int.
     * @return The preference value if it exists and is a valid Int, or [defValue].
     */
    override suspend fun getInt(key: String, defValue: Int): Int {
        return store.readPreference(
            key = key,
            defaultValue = defValue,
            converter = { it.toIntOrNull() }
        )
    }

    /**
     * Retrieves a Long value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or if the stored value is not a valid Long.
     * @return The preference value if it exists and is a valid Long, or [defValue].
     */
    override suspend fun getLong(key: String, defValue: Long): Long {
        return store.readPreference(
            key = key,
            defaultValue = defValue,
            converter = { it.toLongOrNull() }
        )
    }

    /**
     * Retrieves a Float value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or if the stored value is not a valid Float.
     * @return The preference value if it exists and is a valid Float, or [defValue].
     */
    override suspend fun getFloat(key: String, defValue: Float): Float {
        return store.readPreference(
            key = key,
            defaultValue = defValue,
            converter = { it.toFloatOrNull() }
        )
    }

    /**
     * Retrieves a Boolean value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or if the stored value is not a valid Boolean.
     * @return The preference value if it exists and is a valid Boolean, or [defValue].
     */
    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean {
        return store.readPreference(
            key = key,
            defaultValue = defValue,
            converter = { it.toBoolean() }
        )
    }
}