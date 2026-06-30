package com.santimattius.kvs

import com.santimattius.kvs.internal.KvsStandard
import com.santimattius.kvs.internal.KvsStream
import com.santimattius.kvs.WriteKvsException
import com.santimattius.kvs.ttl.CleanupJob
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration

/**
 * Extended key-value storage interface with Time-To-Live (TTL) support.
 *
 * [KvsExtended] augments the base KVS contract with the ability to associate an
 * expiry [Duration] with individual entries. Expired entries are swept by the
 * background [CleanupJob] returned from [cleanupJob].
 *
 * This interface is experimental and guarded by [ExperimentalKvsTtl]. Opt in with
 * `@OptIn(ExperimentalKvsTtl::class)` or the compiler flag `-opt-in=com.santimattius.kvs.ExperimentalKvsTtl`.
 *
 * @see KvsExtendedEditor
 * @see CleanupJob
 * @see ExperimentalKvsTtl
 */
@ExperimentalKvsTtl
interface KvsExtended : KvsStandard, KvsStream {

    /**
     * Builder for applying a batch of write operations with optional TTL to the store.
     *
     * Each `put*` overload comes in two forms: one without a [Duration] (no expiry) and
     * one with a [Duration] (entry expires after the given time). Call [commit] to flush
     * all staged operations atomically.
     *
     * @see KvsExtended.edit
     */
    interface KvsExtendedEditor {

        /**
         * Stages a [String] value without TTL under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The string value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putString(key: String, value: String): KvsExtendedEditor

        /**
         * Stages a [String] value with a TTL of [duration] under [key].
         *
         * The entry is considered expired and eligible for removal after [duration] elapses.
         *
         * @param key Storage key. Must not be blank.
         * @param value The string value to store.
         * @param duration Time after which the entry expires. Must be positive.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putString(key: String, value: String, duration: Duration): KvsExtendedEditor

        /**
         * Stages an [Int] value without TTL under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The integer value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putInt(key: String, value: Int): KvsExtendedEditor

        /**
         * Stages an [Int] value with a TTL of [duration] under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The integer value to store.
         * @param duration Time after which the entry expires. Must be positive.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putInt(key: String, value: Int, duration: Duration): KvsExtendedEditor

        /**
         * Stages a [Long] value without TTL under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The long value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putLong(key: String, value: Long): KvsExtendedEditor

        /**
         * Stages a [Long] value with a TTL of [duration] under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The long value to store.
         * @param duration Time after which the entry expires. Must be positive.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putLong(key: String, value: Long, duration: Duration): KvsExtendedEditor

        /**
         * Stages a [Float] value without TTL under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The float value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putFloat(key: String, value: Float): KvsExtendedEditor

        /**
         * Stages a [Float] value with a TTL of [duration] under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The float value to store.
         * @param duration Time after which the entry expires. Must be positive.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putFloat(key: String, value: Float, duration: Duration): KvsExtendedEditor

        /**
         * Stages a [Boolean] value without TTL under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The boolean value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putBoolean(key: String, value: Boolean): KvsExtendedEditor

        /**
         * Stages a [Boolean] value with a TTL of [duration] under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The boolean value to store.
         * @param duration Time after which the entry expires. Must be positive.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putBoolean(key: String, value: Boolean, duration: Duration): KvsExtendedEditor

        /**
         * Stages the removal of the entry identified by [key].
         *
         * If [key] does not exist the operation is a no-op.
         *
         * @param key Storage key of the entry to remove.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun remove(key: String): KvsExtendedEditor

        /**
         * Stages the removal of ALL entries from the store (including TTL metadata).
         *
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun clear(): KvsExtendedEditor

        /**
         * Flushes all staged operations atomically to the underlying storage.
         *
         * @throws IllegalStateException if [commit] has already been called on this editor.
         * @throws WriteKvsException if the underlying storage layer reports a write failure.
         * @throws CancellationException if the calling coroutine is cancelled before the
         *   write completes.
         */
        @Throws(IllegalStateException::class, WriteKvsException::class, CancellationException::class)
        suspend fun commit()
    }

    /**
     * Returns a new [KvsExtendedEditor] for staging write operations with optional TTL.
     *
     * @return A new [KvsExtendedEditor] bound to this store.
     */
    fun edit(): KvsExtendedEditor

    /**
     * Returns `true` if an entry with the given [key] exists and has not expired.
     *
     * @param key The key to look up.
     * @return `true` if the key is present and not expired, `false` otherwise.
     * @throws CancellationException if the calling coroutine is cancelled.
     */
    suspend operator fun contains(key: String): Boolean

    /**
     * Returns a [CleanupJob] that, when started, periodically scans the store and
     * removes entries whose TTL has elapsed.
     *
     * The job runs on the background dispatcher and sweeps at the given [interval].
     * Cancel the returned [CleanupJob] (or its parent scope) to stop sweeping.
     *
     * @param interval How often the cleanup sweep runs. Must be positive.
     * @return A [CleanupJob] ready to be started.
     * @see CleanupJob
     */
    fun cleanupJob(interval: Duration): CleanupJob
}
