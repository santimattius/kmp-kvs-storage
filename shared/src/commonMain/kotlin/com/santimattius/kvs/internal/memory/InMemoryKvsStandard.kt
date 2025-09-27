package com.santimattius.kvs.internal.memory

import com.santimattius.kvs.internal.datastore.KvsStandard
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * An in-memory implementation of the [KvsStandard] interface.
 *
 * This class provides synchronous access to key-value pairs stored in an [InMemoryPreferences]
 * instance. All read operations are synchronized using the provided [lock] to ensure thread safety.
 *
 * @property preferences The [InMemoryPreferences] instance holding the data.
 * @property lock A [SynchronizedObject] used to protect access to the underlying preferences data.
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal class InMemoryKvsStandard(
    private val preferences: InMemoryPreferences,
    private val lock: SynchronizedObject
) : KvsStandard {

    /**
     * Retrieves all key-value pairs stored.
     * This operation is synchronized.
     *
     * @return A map containing all stored preferences. The values are of type [Any],
     * reflecting the type they were stored with.
     */
    override suspend fun getAll(): Map<String, Any> = synchronized(lock) {
        preferences.values
    }

    /**
     * Retrieves a String value from the storage.
     * This operation is synchronized.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is not a String.
     * @return The preference value if it exists and is a String, or [defValue].
     */
    override suspend fun getString(key: String, defValue: String): String = synchronized(lock) {
        preferences.get(key, defValue) as? String ?: defValue
    }

    /**
     * Retrieves an Int value from the storage.
     * This operation is synchronized.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is not an Int.
     * @return The preference value if it exists and is an Int, or [defValue].
     */
    override suspend fun getInt(key: String, defValue: Int): Int = synchronized(lock) {
        preferences.get(key, defValue) as? Int ?: defValue
    }

    /**
     * Retrieves a Long value from the storage.
     * This operation is synchronized.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is not a Long.
     * @return The preference value if it exists and is a Long, or [defValue].
     */
    override suspend fun getLong(key: String, defValue: Long): Long = synchronized(lock) {
        preferences.get(key, defValue) as? Long ?: defValue
    }

    /**
     * Retrieves a Float value from the storage.
     * This operation is synchronized.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is not a Float.
     * @return The preference value if it exists and is a Float, or [defValue].
     */
    override suspend fun getFloat(key: String, defValue: Float): Float = synchronized(lock) {
        preferences.get(key, defValue) as? Float ?: defValue
    }

    /**
     * Retrieves a Boolean value from the storage.
     * This operation is synchronized.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist or is not a Boolean.
     * @return The preference value if it exists and is a Boolean, or [defValue].
     */
    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean = synchronized(lock) {
        preferences.get(key, defValue) as? Boolean ?: defValue
    }

}