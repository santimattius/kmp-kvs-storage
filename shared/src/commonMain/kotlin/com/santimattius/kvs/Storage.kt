package com.santimattius.kvs

import com.santimattius.kvs.internal.DataStoreKvs
import com.santimattius.kvs.internal.provideDataStoreInstance
import com.santimattius.kvs.internal.provideInMemoryKvsInstance

/**
 * Entry point for accessing Key-Value Storage (KVS) instances.
 * This object provides factory methods to obtain [Kvs] implementations.
 */
object Storage {

    /**
     * Creates or retrieves a named [Kvs] instance.
     *
     * This function provides an implementation of the [Kvs] interface,
     * typically backed by DataStore. Each unique [name] will correspond
     * to a distinct DataStore file, ensuring data isolation between different
     * KVS instances.
     *
     * @param name The unique name for the KVS instance. This name is often
     *             used as the filename for the underlying DataStore.
     * @return A [Kvs] instance associated with the given [name].
     */
    fun kvs(name: String): Kvs = DataStoreKvs(provideDataStoreInstance(name))
}


fun Storage.inMemory(name: String): Kvs = provideInMemoryKvsInstance(name)