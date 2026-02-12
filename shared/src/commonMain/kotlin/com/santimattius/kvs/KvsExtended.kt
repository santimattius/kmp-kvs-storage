package com.santimattius.kvs

import com.santimattius.kvs.internal.datastore.KvsStandard
import com.santimattius.kvs.internal.datastore.KvsStream
import com.santimattius.kvs.internal.exception.WriteKvsException
import com.santimattius.kvs.internal.ttl.cleanup.CleanupJob
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration

/**
 * Represents a Key-Value Storage system with Time-To-Live (TTL) support.
 *
 * **Experimental:** This interface is part of the experimental TTL feature. The API may change
 * in future releases. Opt in with `@OptIn(ExperimentalKvsTtl::class)` when using [KvsExtended].
 *
 * This interface extends [KvsStandard] and [KvsStream] to provide automatic expiration
 * of stored keys based on TTL configuration. Keys can have individual TTL values or use
 * a default TTL configured at the instance level.
 *
 * **TTL Features:**
 * - Default TTL: Configured at instance creation, applies to all keys without explicit TTL
 * - Per-key TTL: Override default TTL for specific keys when storing values
 * - Automatic expiration: Expired keys are automatically filtered and cleaned up
 * - Lazy cleanup: Keys are removed when accessed or via background cleanup job
 *
 * **Usage:**
 * ```
 * val kvs = Storage.kvs("cache", ttl = myTtl)
 * kvs.edit()
 *     .putString("key1", "value1")  // Uses default TTL
 *     .putString("key2", "value2", Duration.ofMinutes(30))  // Override TTL
 *     .commit()
 * ```
 */
@ExperimentalKvsTtl
interface KvsExtended : KvsStandard, KvsStream {

    /**
     * An editor for modifying values in the Key-Value Storage with TTL support.
     *
     * This interface extends the standard editor functionality to support Time-To-Live (TTL)
     * configuration for individual keys. Changes made through the editor are applied atomically
     * when [commit] is called.
     *
     * **TTL Behavior:**
     * - Methods without [Duration] parameter use the default TTL (if configured)
     * - Methods with [Duration] parameter override the default TTL for that specific key
     * - If no default TTL is configured and no [Duration] is provided, the key will not expire
     */
    interface KvsExtendedEditor {

        /**
         * Sets a String value in the editor.
         *
         * If no default TTL is configured, the key will not expire.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putString(key: String, value: String): KvsExtendedEditor

        /**
         * Sets a String value in the editor with a specific TTL duration.
         *
         * This method overrides any default TTL configured at the instance level.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @param duration The TTL duration for this specific key.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putString(key: String, value: String, duration: Duration): KvsExtendedEditor

        /**
         * Sets an Int value in the editor.
         *
         * If no default TTL is configured, the key will not expire.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putInt(key: String, value: Int): KvsExtendedEditor

        /**
         * Sets an Int value in the editor with a specific TTL duration.
         *
         * This method overrides any default TTL configured at the instance level.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @param duration The TTL duration for this specific key.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putInt(key: String, value: Int, duration: Duration): KvsExtendedEditor

        /**
         * Sets a Long value in the editor.
         *
         * If no default TTL is configured, the key will not expire.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putLong(key: String, value: Long): KvsExtendedEditor

        /**
         * Sets a Long value in the editor with a specific TTL duration.
         *
         * This method overrides any default TTL configured at the instance level.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @param duration The TTL duration for this specific key.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putLong(key: String, value: Long, duration: Duration): KvsExtendedEditor

        /**
         * Sets a Float value in the editor.
         *
         * If no default TTL is configured, the key will not expire.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putFloat(key: String, value: Float): KvsExtendedEditor

        /**
         * Sets a Float value in the editor with a specific TTL duration.
         *
         * This method overrides any default TTL configured at the instance level.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @param duration The TTL duration for this specific key.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putFloat(key: String, value: Float, duration: Duration): KvsExtendedEditor

        /**
         * Sets a Boolean value in the editor.
         *
         * If no default TTL is configured, the key will not expire.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putBoolean(key: String, value: Boolean): KvsExtendedEditor

        /**
         * Sets a Boolean value in the editor with a specific TTL duration.
         *
         * This method overrides any default TTL configured at the instance level.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @param duration The TTL duration for this specific key.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putBoolean(key: String, value: Boolean, duration: Duration): KvsExtendedEditor

        /**
         * Removes a preference value from the editor.
         *
         * @param key The name of the preference to remove.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun remove(key: String): KvsExtendedEditor

        /**
         * Removes all preference values from the editor.
         *
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun clear(): KvsExtendedEditor

        /**
         * Commits the preference changes to persistent storage.
         * This operation is performed asynchronously.
         */
        @Throws(
            IllegalStateException::class,
            WriteKvsException::class,
            CancellationException::class
        )
        suspend fun commit()

    }

    /**
     * Creates a new [KvsExtendedEditor] for modifying preferences.
     *
     * @return A new [KvsExtendedEditor] instance.
     */
    fun edit(): KvsExtendedEditor

    /**
     * Checks if the storage contains a non-expired preference with the given key.
     *
     * This method verifies both the existence of the key and whether it has expired.
     * Expired keys are considered as not present.
     *
     * @param key The key to check.
     * @return `true` if the key exists and has not expired, `false` otherwise.
     */
    suspend operator fun contains(key: String): Boolean

    /**
     * Creates a background cleanup job for expired keys.
     *
     * The cleanup job will periodically scan and remove expired keys from storage.
     * This is useful for high-volume scenarios where keys may not be accessed frequently.
     *
     * @param interval The interval between cleanup runs.
     * @return A [CleanupJob] instance that can be started with a [kotlinx.coroutines.CoroutineScope].
     *
     * @sample
     * ```
     * val cleanupJob = kvs.cleanupJob(Duration.ofMinutes(10))
     * val job = cleanupJob.start(coroutineScope)
     * // Later, to cancel:
     * job.cancel()
     * ```
     */
    fun cleanupJob(interval: Duration): CleanupJob

}