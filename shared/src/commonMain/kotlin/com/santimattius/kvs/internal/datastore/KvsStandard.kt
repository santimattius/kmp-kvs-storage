package com.santimattius.kvs.internal.datastore

/**
 * Represents a Key-Value Storage system.
 * This interface provides methods to store, retrieve, and manage key-value pairs.
 */
interface KvsStandard {

    /**
     * Retrieves all key-value pairs stored.
     *
     * @return A map containing all stored preferences. The values are of type [Any],
     * reflecting the type they were stored with (e.g., String, Int, Boolean).
     */
    suspend fun getAll(): Map<String, Any>

    /**
     * Retrieves a String value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getString(key: String, defValue: String): String

    /**
     * Retrieves an Int value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getInt(key: String, defValue: Int): Int

    /**
     * Retrieves a Long value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getLong(key: String, defValue: Long): Long

    /**
     * Retrieves a Float value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getFloat(key: String, defValue: Float): Float

    /**
     * Retrieves a Boolean value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getBoolean(key: String, defValue: Boolean): Boolean
}