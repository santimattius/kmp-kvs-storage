package com.santimattius.kvs.internal.datastore

import com.santimattius.kvs.internal.exception.GetAllKvsException
import com.santimattius.kvs.internal.exception.KvsException
import com.santimattius.kvs.internal.exception.ReadKvsException
import kotlinx.coroutines.CancellationException

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
     * @throws GetAllKvsException if there is an issue retrieving all key-value pairs.
     * @throws KvsException if there is a general issue with the key-value store.
     */
    @Throws(GetAllKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getAll(): Map<String, Any>

    /**
     * Retrieves a String value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     * @throws ReadKvsException if there is an error reading from the storage.
     * @throws KvsException for other storage-related errors.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getString(key: String, defValue: String): String

    /**
     * Retrieves an Int value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     * @throws ReadKvsException if there is an error reading from the storage.
     * @throws KvsException if there is a general Key-Value Storage error.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getInt(key: String, defValue: Int): Int

    /**
     * Retrieves a Long value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     * @throws ReadKvsException if there is an error reading from the storage.
     * @throws KvsException if a general KVS error occurs.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getLong(key: String, defValue: Long): Long

    /**
     * Retrieves a Float value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     * @throws ReadKvsException If an error occurs while reading from the storage.
     * @throws KvsException If a general KVS error occurs.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getFloat(key: String, defValue: Float): Float

    /**
     * Retrieves a Boolean value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     * @throws ReadKvsException If an error occurs while reading from the storage.
     * @throws KvsException If a general KVS error occurs.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getBoolean(key: String, defValue: Boolean): Boolean
}