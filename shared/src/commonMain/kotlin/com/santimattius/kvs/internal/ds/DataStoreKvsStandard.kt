package com.santimattius.kvs.internal.ds

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Provides standard (non-Flow) access to DataStore preferences.
 *
 * This class implements the [KvsStandard] interface, offering methods to synchronously
 * retrieve various data types from a [DataStore]. It's designed for use cases where
 * a single, current value is needed rather than a stream of updates.
 *
 * @property dataStore The [DataStore] instance used for data persistence.
 * @property dispatcher The [CoroutineDispatcher] used for background operations, defaults to [Dispatchers.IO].
 */
class DataStoreKvsStandard(
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : KvsStandard {

    /**
     * Retrieves all key-value pairs stored in the DataStore.
     *
     * @return A map containing all stored preferences. The keys are the preference names,
     *         and the values are of type [Any], reflecting their original stored type.
     */
    override suspend fun getAll(): Map<String, Any> {
        val currentPreferences = dataStore.data.first()
        return currentPreferences.asMap().mapKeys { entry ->
            entry.key.name
        }
    }

    /**
     * Reads a preference value associated with the given [key].
     *
     * This is a generic private function used by the public getter methods.
     * It retrieves a string preference and then uses the provided [converter]
     * to transform it into the desired type [T].
     *
     * @param T The target data type.
     * @param key The preference key.
     * @param defaultValue The value to return if the key is not found or conversion fails.
     * @param converter A function to convert the String value to type [T].
     * @return The converted preference value, or [defaultValue] if not found or conversion fails.
     */
    private suspend fun <T> readPreference(
        key: String,
        defaultValue: T,
        converter: (String) -> T?
    ): T = withContext(dispatcher){
        val preferencesKey = stringPreferencesKey(key)
        // Use data.first() to get the current snapshot of preferences.
        // Using .last() on DataStore's Flow would suspend indefinitely.
        val currentPreferences = dataStore.data.first()
        val stringValue = currentPreferences[preferencesKey]
        if (stringValue != null) {
            converter(stringValue) ?: defaultValue
        } else {
            defaultValue
        }
    }

    /**
     * Retrieves a String value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    override suspend fun getString(key: String, defValue: String): String {
        return readPreference(key, defValue) { it }
    }

    /**
     * Retrieves an Int value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or if the stored value is not a valid Int.
     * @return The preference value if it exists and is a valid Int, or [defValue].
     */
    override suspend fun getInt(key: String, defValue: Int): Int {
        return readPreference(key, defValue) { it.toIntOrNull() }
    }

    /**
     * Retrieves a Long value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or if the stored value is not a valid Long.
     * @return The preference value if it exists and is a valid Long, or [defValue].
     */
    override suspend fun getLong(key: String, defValue: Long): Long {
        return readPreference(key, defValue) { it.toLongOrNull() }
    }

    /**
     * Retrieves a Float value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or if the stored value is not a valid Float.
     * @return The preference value if it exists and is a valid Float, or [defValue].
     */
    override suspend fun getFloat(key: String, defValue: Float): Float {
        return readPreference(key, defValue) { it.toFloatOrNull() }
    }

    /**
     * Retrieves a Boolean value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or if the stored value is not a valid Boolean.
     * @return The preference value if it exists and is a valid Boolean, or [defValue].
     */
    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean {
        return readPreference(key, defValue) { it.toBooleanStrictOrNull() }
    }
}