package com.santimattius.kvs.internal

import com.santimattius.kvs.GetAllKvsException
import com.santimattius.kvs.InternalKvsApi
import com.santimattius.kvs.KvsException
import com.santimattius.kvs.ReadKvsException
import kotlinx.coroutines.CancellationException

/**
 * Internal contract for typed read operations on a key-value store.
 *
 * This interface is part of the KvsStorage internal API. It is NOT intended for
 * direct use by library consumers. Stability is NOT guaranteed across versions.
 *
 * All operations are suspending and honour structured concurrency — a cancelled
 * coroutine receives [CancellationException] immediately.
 *
 * @see com.santimattius.kvs.Kvs
 * @see com.santimattius.kvs.KvsExtended
 */
@InternalKvsApi
interface KvsStandard {

    /**
     * Returns all key-value entries currently held in the store.
     *
     * @return A snapshot map of all entries. May be empty; never `null`.
     * @throws GetAllKvsException if the underlying storage fails to retrieve entries.
     * @throws KvsException for any other storage-layer error.
     * @throws CancellationException if the calling coroutine is cancelled.
     */
    @Throws(GetAllKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getAll(): Map<String, Any>

    /**
     * Reads the [String] value stored under [key], returning [defValue] if absent.
     *
     * @param key Storage key to look up.
     * @param defValue Value returned when [key] is not found.
     * @return The stored string, or [defValue].
     * @throws ReadKvsException if the stored value cannot be read or cast to [String].
     * @throws KvsException for any other storage-layer error.
     * @throws CancellationException if the calling coroutine is cancelled.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getString(key: String, defValue: String): String

    /**
     * Reads the [Int] value stored under [key], returning [defValue] if absent.
     *
     * @param key Storage key to look up.
     * @param defValue Value returned when [key] is not found.
     * @return The stored integer, or [defValue].
     * @throws ReadKvsException if the stored value cannot be read or cast to [Int].
     * @throws KvsException for any other storage-layer error.
     * @throws CancellationException if the calling coroutine is cancelled.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getInt(key: String, defValue: Int): Int

    /**
     * Reads the [Long] value stored under [key], returning [defValue] if absent.
     *
     * @param key Storage key to look up.
     * @param defValue Value returned when [key] is not found.
     * @return The stored long, or [defValue].
     * @throws ReadKvsException if the stored value cannot be read or cast to [Long].
     * @throws KvsException for any other storage-layer error.
     * @throws CancellationException if the calling coroutine is cancelled.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getLong(key: String, defValue: Long): Long

    /**
     * Reads the [Float] value stored under [key], returning [defValue] if absent.
     *
     * @param key Storage key to look up.
     * @param defValue Value returned when [key] is not found.
     * @return The stored float, or [defValue].
     * @throws ReadKvsException if the stored value cannot be read or cast to [Float].
     * @throws KvsException for any other storage-layer error.
     * @throws CancellationException if the calling coroutine is cancelled.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getFloat(key: String, defValue: Float): Float

    /**
     * Reads the [Boolean] value stored under [key], returning [defValue] if absent.
     *
     * @param key Storage key to look up.
     * @param defValue Value returned when [key] is not found.
     * @return The stored boolean, or [defValue].
     * @throws ReadKvsException if the stored value cannot be read or cast to [Boolean].
     * @throws KvsException for any other storage-layer error.
     * @throws CancellationException if the calling coroutine is cancelled.
     */
    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getBoolean(key: String, defValue: Boolean): Boolean
}
