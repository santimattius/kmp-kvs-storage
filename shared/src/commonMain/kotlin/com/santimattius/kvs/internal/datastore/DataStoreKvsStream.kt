package com.santimattius.kvs.internal.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.santimattius.kvs.internal.datastore.storage.Storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

/**
 * Provides Flow-based access to DataStore preferences.
 *
 * This class implements the [KvsStream] interface, offering methods to observe
 * changes to various data types stored in a [DataStore] as a [Flow].
 * It ensures that data observation occurs on the specified [dispatcher].
 *
 * @property store The [DataStore] instance used for data persistence.
 */
internal class DataStoreKvsStream(
    private val store: Storage<String>,
) : KvsStream {

    /**
     * Creates a [Flow] that emits updates to a preference value.
     *
     * This is a generic private function used by the public getter Flow methods.
     * It maps over the `dataStore.data` Flow, converting each emitted [Preferences]
     * snapshot into the desired type [T] using the [converter].
     * The Flow operations are performed on the specified [dispatcher].
     *
     * @param T The target data type.
     * @param key The preference key.
     * @param defaultValue The value to emit if the key is not found or conversion fails.
     * @param converter A function to convert the String value to type [T].
     * @return A [Flow] emitting the preference value, or [defaultValue].
     */
    private fun <T> readPreferenceFlow(
        key: String,
        defaultValue: T,
        converter: (String) -> T?
    ) = store.readPreferenceAsStream(
        key = key,
        defaultValue = defaultValue,
        converter = converter
    )

    /**
     * Returns a [Flow] that emits a map of all key-value pairs upon any change.
     *
     * @return A [Flow] emitting a map of all preferences. Keys are preference names,
     *         and values are of type [Any], reflecting their original stored type.
     */
    override fun getAllAsStream(): Flow<Map<String, Any>> {
        return store.getAllAsStream()
    }

    /**
     * Returns a [Flow] that emits updates to a String preference value.
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist.
     * @return A [Flow] emitting the String preference value, or [defValue].
     */
    override fun getStringAsStream(
        key: String,
        defValue: String
    ): Flow<String> {
        return readPreferenceFlow(key, defValue) { value -> value }
    }

    /**
     * Returns a [Flow] that emits updates to an Int preference value.
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is not a valid Int.
     * @return A [Flow] emitting the Int preference value, or [defValue].
     */
    override fun getIntAsStream(
        key: String,
        defValue: Int
    ): Flow<Int> {
        return readPreferenceFlow(key, defValue) { value -> value.toIntOrNull() }
    }

    /**
     * Returns a [Flow] that emits updates to a Long preference value.
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is not a valid Long.
     * @return A [Flow] emitting the Long preference value, or [defValue].
     */
    override fun getLongAsStream(
        key: String,
        defValue: Long
    ): Flow<Long> {
        return readPreferenceFlow(key, defValue) { value -> value.toLongOrNull() }
    }

    /**
     * Returns a [Flow] that emits updates to a Float preference value.
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is not a valid Float.
     * @return A [Flow] emitting the Float preference value, or [defValue].
     */
    override fun getFloatAsStream(
        key: String,
        defValue: Float
    ): Flow<Float> {
        return readPreferenceFlow(key, defValue) { value -> value.toFloatOrNull() }
    }

    /**
     * Returns a [Flow] that emits updates to a Boolean preference value.
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is not a valid Boolean.
     * @return A [Flow] emitting the Boolean preference value, or [defValue].
     */
    override fun getBooleanAsStream(
        key: String,
        defValue: Boolean
    ): Flow<Boolean> {
        return readPreferenceFlow(key, defValue) { value -> value.toBooleanStrictOrNull() }
    }
}