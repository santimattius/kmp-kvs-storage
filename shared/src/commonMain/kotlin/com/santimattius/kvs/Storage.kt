package com.santimattius.kvs

import com.santimattius.kvs.internal.DataStoreKvs
import com.santimattius.kvs.internal.datastore.encrypt.DsEncryptStorage
import com.santimattius.kvs.internal.datastore.encrypt.Encryptor
import com.santimattius.kvs.internal.datastore.encrypt.encryptor
import com.santimattius.kvs.internal.datastore.storage.DsStorage
import com.santimattius.kvs.internal.provideDataStoreInstance
import com.santimattius.kvs.internal.provideInMemoryKvsInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

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
    fun kvs(name: String): Kvs = DataStoreKvs(
        dataStore = DsStorage(
            dataStore = provideDataStoreInstance(name),
            dispatcher = Dispatchers.IO
        )
    )

    fun encryptKvs(name: String): Kvs = DataStoreKvs(
        dataStore = DsEncryptStorage(
            dataStore = provideDataStoreInstance(name),
            encryptor = encryptor(),
            dispatcher = Dispatchers.IO
        )
    )


    /**
     * Creates or retrieves a named in-memory [Kvs] instance.
     *
     * This function provides an in-memory implementation of the [Kvs] interface.
     * Data stored in this instance will not persist across application restarts.
     * Each unique [name] will correspond to a distinct in-memory store.
     * This is primarily useful for testing or scenarios where data persistence
     * is not required.
     *
     * @param name The unique name for the in-memory KVS instance.
     * @return An in-memory [Kvs] instance associated with the given [name].
     */
    fun inMemoryKvs(name: String): Kvs = provideInMemoryKvsInstance(name)
}