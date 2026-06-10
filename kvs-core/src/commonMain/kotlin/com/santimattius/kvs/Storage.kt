package com.santimattius.kvs

import com.santimattius.kvs.internal.provideInMemoryKvsInstance

/**
 * Entry point for Key-Value Storage instances.
 *
 * Disk persistence factories (`kvsLight`, `kvsOptimized`, `document`) are provided
 * as extensions by their respective artifacts — see README and MIGRATION.md.
 */
object Storage {

    private var isDebug = false

    fun debug(isDebug: Boolean) {
        this.isDebug = isDebug
    }

    /**
     * Returns an in-memory [Kvs] that does not persist to disk.
     * Requires only `kvs-core` on the classpath.
     */
    fun inMemoryKvs(name: String): Kvs = provideInMemoryKvsInstance(name)
}
