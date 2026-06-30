package com.santimattius.kvs.internal

import com.santimattius.kvs.InternalKvsApi
import kotlinx.coroutines.flow.Flow

/**
 * Internal contract for reactive (Flow-based) read operations on a key-value store.
 *
 * This interface is part of the KvsStorage internal API. It is NOT intended for
 * direct use by library consumers. Stability is NOT guaranteed across versions.
 *
 * Each method returns a cold [Flow] that emits the current value immediately on
 * collection and subsequently emits a new value whenever the underlying entry changes.
 *
 * @see com.santimattius.kvs.Kvs
 * @see com.santimattius.kvs.KvsExtended
 */
@InternalKvsApi
interface KvsStream {

    /**
     * Returns a [Flow] that emits the full store snapshot on every change.
     *
     * The flow emits the current state on subscription and a new snapshot on every
     * write that touches any key.
     *
     * @return A cold [Flow] of key-value snapshots.
     */
    fun getAllAsStream(): Flow<Map<String, Any>>

    /**
     * Returns a [Flow] of the [String] value stored under [key].
     *
     * Emits [defValue] when [key] is absent or has been removed.
     *
     * @param key Storage key to observe.
     * @param defValue Emitted when [key] is not present.
     * @return A cold [Flow] that emits the current value and subsequent updates.
     */
    fun getStringAsStream(key: String, defValue: String): Flow<String>

    /**
     * Returns a [Flow] of the [Int] value stored under [key].
     *
     * Emits [defValue] when [key] is absent or has been removed.
     *
     * @param key Storage key to observe.
     * @param defValue Emitted when [key] is not present.
     * @return A cold [Flow] that emits the current value and subsequent updates.
     */
    fun getIntAsStream(key: String, defValue: Int): Flow<Int>

    /**
     * Returns a [Flow] of the [Long] value stored under [key].
     *
     * Emits [defValue] when [key] is absent or has been removed.
     *
     * @param key Storage key to observe.
     * @param defValue Emitted when [key] is not present.
     * @return A cold [Flow] that emits the current value and subsequent updates.
     */
    fun getLongAsStream(key: String, defValue: Long): Flow<Long>

    /**
     * Returns a [Flow] of the [Float] value stored under [key].
     *
     * Emits [defValue] when [key] is absent or has been removed.
     *
     * @param key Storage key to observe.
     * @param defValue Emitted when [key] is not present.
     * @return A cold [Flow] that emits the current value and subsequent updates.
     */
    fun getFloatAsStream(key: String, defValue: Float): Flow<Float>

    /**
     * Returns a [Flow] of the [Boolean] value stored under [key].
     *
     * Emits [defValue] when [key] is absent or has been removed.
     *
     * @param key Storage key to observe.
     * @param defValue Emitted when [key] is not present.
     * @return A cold [Flow] that emits the current value and subsequent updates.
     */
    fun getBooleanAsStream(key: String, defValue: Boolean): Flow<Boolean>
}
