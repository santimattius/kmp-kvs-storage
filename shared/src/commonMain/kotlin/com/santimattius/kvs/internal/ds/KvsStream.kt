package com.santimattius.kvs.internal.ds

import kotlinx.coroutines.flow.Flow

/**
 * Represents a Key-Value Storage system.
 * This interface provides methods to store, retrieve, and manage key-value pairs.
 */
interface KvsStream {
    
    /**
     * Retrieves all key-value pairs stored.
     *
     * @return A map containing all stored preferences. The values are of type [Any],
     * reflecting the type they were stored with (e.g., String, Int, Boolean).
     */
     fun getAllAsStream(): Flow<Map<String, Any>>

    /**
     * Retrieves a String value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
     fun getStringAsStream(key: String, defValue: String): Flow<String>

    /**
     * Retrieves an Int value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
     fun getIntAsStream(key: String, defValue: Int): Flow<Int>

    /**
     * Retrieves a Long value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
     fun getLongAsStream(key: String, defValue: Long): Flow<Long>

    /**
     * Retrieves a Float value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
     fun getFloatAsStream(key: String, defValue: Float): Flow<Float>

    /**
     * Retrieves a Boolean value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
     fun getBooleanAsStream(key: String, defValue: Boolean): Flow<Boolean>
    
}