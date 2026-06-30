package com.santimattius.kvs

import com.santimattius.kvs.internal.KvsStandard
import com.santimattius.kvs.internal.KvsStream
import com.santimattius.kvs.WriteKvsException
import kotlinx.coroutines.CancellationException

/**
 * Core key-value storage interface.
 *
 * [Kvs] is the primary contract for reading and writing typed key-value pairs.
 * All read operations are suspending and safe to call from any coroutine context.
 * Writes are batched through [KvsEditor] and flushed atomically via [KvsEditor.commit].
 *
 * Implementations are provided by the artifact-specific factory functions on [Storage]
 * (e.g. `Storage.inMemoryKvs`, `Storage.kvsLight`, `Storage.kvsOptimized`).
 *
 * @see KvsEditor
 * @see Storage
 */
interface Kvs : KvsStandard, KvsStream {

    /**
     * Builder for applying a batch of write operations to the store.
     *
     * Call [edit] to obtain an instance, chain the desired mutations, then call
     * [commit] to flush them atomically to the underlying storage.
     *
     * Each mutation method returns `this` to support a fluent call chain:
     * ```kotlin
     * kvs.edit()
     *    .putString("theme", "dark")
     *    .putBoolean("notifications", true)
     *    .commit()
     * ```
     *
     * @see Kvs.edit
     */
    interface KvsEditor {

        /**
         * Stages a [String] value to be persisted under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The string value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putString(key: String, value: String): KvsEditor

        /**
         * Stages an [Int] value to be persisted under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The integer value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putInt(key: String, value: Int): KvsEditor

        /**
         * Stages a [Long] value to be persisted under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The long value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putLong(key: String, value: Long): KvsEditor

        /**
         * Stages a [Float] value to be persisted under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The float value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putFloat(key: String, value: Float): KvsEditor

        /**
         * Stages a [Boolean] value to be persisted under [key].
         *
         * @param key Storage key. Must not be blank.
         * @param value The boolean value to store.
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun putBoolean(key: String, value: Boolean): KvsEditor

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
        fun remove(key: String): KvsEditor

        /**
         * Stages the removal of ALL entries from the store.
         *
         * @return This editor instance for chaining.
         * @throws IllegalStateException if the editor has already been committed.
         */
        @Throws(IllegalStateException::class)
        fun clear(): KvsEditor

        /**
         * Flushes all staged operations atomically to the underlying storage.
         *
         * This is a suspending call; it must be invoked from a coroutine. The
         * coroutine will be suspended until the write completes or fails.
         *
         * @throws IllegalStateException if [commit] has already been called on this editor.
         * @throws WriteKvsException if the underlying storage layer reports a write failure.
         * @throws CancellationException if the calling coroutine is cancelled before the write
         *   completes. Structured concurrency is respected — the exception propagates normally.
         */
        @Throws(IllegalStateException::class, WriteKvsException::class, CancellationException::class)
        suspend fun commit()
    }

    /**
     * Returns a new [KvsEditor] for staging write operations.
     *
     * Each call returns a fresh editor. Editors are NOT thread-safe; use one editor per
     * coroutine and call [KvsEditor.commit] exactly once.
     *
     * @return A new [KvsEditor] bound to this store.
     */
    fun edit(): KvsEditor

    /**
     * Returns `true` if an entry with the given [key] exists in the store.
     *
     * @param key The key to look up.
     * @return `true` if the key is present, `false` otherwise.
     * @throws CancellationException if the calling coroutine is cancelled.
     */
    suspend operator fun contains(key: String): Boolean
}
