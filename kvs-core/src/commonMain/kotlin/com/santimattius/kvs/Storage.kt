package com.santimattius.kvs

import com.santimattius.kvs.internal.provideInMemoryKvsInstance

/**
 * Entry point for all Key-Value Storage instances.
 *
 * Use the factory methods on this object to obtain a [Kvs] or [KvsExtended] instance:
 *
 * - [inMemoryKvs] — volatile, no disk I/O; available from `kvs-core` alone.
 * - `Storage.kvsLight(...)` — DataStore-backed; provided by the `kvs-persistence-light` artifact.
 * - `Storage.kvsOptimized(...)` — SQLDelight-backed; provided by `kvs-persistence-optimized`.
 * - `Storage.document(...)` — single-document JSON storage; provided by `kvs-document`.
 *
 * Call [debug] before creating any instance to enable verbose logging during development.
 *
 * ```kotlin
 * Storage.debug(BuildConfig.DEBUG)
 * val kvs = Storage.inMemoryKvs("session")
 * ```
 */
object Storage {

    private var isDebug = false

    /**
     * Enables or disables verbose debug logging for all [Storage] backends.
     *
     * Call this once at application startup, before creating any KVS instance.
     * Has no effect on already-created instances.
     *
     * @param isDebug `true` to enable logging, `false` (default) to suppress it.
     */
    fun debug(isDebug: Boolean) {
        this.isDebug = isDebug
    }

    /**
     * Returns an in-memory [Kvs] instance identified by [name].
     *
     * Data is held in a plain in-process map and is lost when the process ends.
     * This implementation requires only the `kvs-core` artifact on the classpath.
     *
     * Multiple calls with the same [name] may or may not return the same instance —
     * consumers should not rely on identity equality.
     *
     * @param name Logical name for this store. Used to distinguish multiple in-memory
     *   instances within the same process.
     * @return A [Kvs] backed by an in-memory map.
     */
    fun inMemoryKvs(name: String): Kvs = provideInMemoryKvsInstance(name)
}
