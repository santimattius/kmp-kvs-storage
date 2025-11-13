package com.santimattius.kvs

import com.santimattius.kvs.internal.DataStoreKvs
import com.santimattius.kvs.internal.datastore.encrypt.DsEncryptStorage
import com.santimattius.kvs.internal.datastore.encrypt.Encryptor
import com.santimattius.kvs.internal.datastore.encrypt.encryptor
import com.santimattius.kvs.internal.datastore.storage.DsStorage
import com.santimattius.kvs.internal.document.DataStoreDocument
import com.santimattius.kvs.internal.document.provideDocumentDataStoreInstance
import com.santimattius.kvs.internal.logger.NoopKvsLogger
import com.santimattius.kvs.internal.logger.logger
import com.santimattius.kvs.internal.provideDataStoreInstance
import com.santimattius.kvs.internal.provideInMemoryKvsInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Entry point for accessing Key-Value Storage (KVS) instances.
 * This object provides factory methods to obtain [Kvs] implementations.
 */
object Storage {

    private var isDebug = false

    /**
     * Enables or disables debug logging for KVS operations.
     *
     * When debug mode is enabled, KVS operations may produce additional logging output,
     * which can be helpful for troubleshooting and understanding the internal workings
     * of the storage system.
     *
     * @param isDebug `true` to enable debug logging, `false` to disable it.
     */
    fun debug(isDebug: Boolean) {
        this.isDebug = isDebug
    }

    private fun getLogger() = if (isDebug) logger() else NoopKvsLogger

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

    /**
     * Creates or retrieves a named, encrypted [Kvs] instance.
     *
     * This function provides an implementation of the [Kvs] interface that
     * encrypts data before storing it, typically using DataStore as the
     * underlying persistence mechanism. Each unique [name] will correspond
     * to a distinct, encrypted DataStore file.
     *
     * @param name The unique name for the encrypted KVS instance. This name is
     *             often used as the filename for the underlying DataStore.
     * @param key The encryption key used to secure the data. It is crucial
     *            to manage this key securely.
     * @return An encrypted [Kvs] instance associated with the given [name]
     *         and [key].
     */
    fun encryptKvs(name: String, key: String): Kvs {
        return encryptKvs(
            name = name,
            encryptor = encryptor(
                key = key,
                logger = getLogger()
            )
        )
    }

    /**
     * Creates or retrieves a named, encrypted [Kvs] instance.
     *
     * This function provides an implementation of the [Kvs] interface that
     * encrypts data before storing it, typically using DataStore as the backend.
     * Each unique [name] will correspond to a distinct, encrypted DataStore file.
     *
     * @param name The unique name for the encrypted KVS instance. This name is often
     *             used as the filename for the underlying DataStore.
     * @param encryptor The [Encryptor] implementation to be used for data encryption
     *                  and decryption.
     * @return An encrypted [Kvs] instance associated with the given [name] and [encryptor].
     */
    fun encryptKvs(name: String, encryptor: Encryptor): Kvs = DataStoreKvs(
        dataStore = DsEncryptStorage(
            dataStore = provideDataStoreInstance(name),
            encryptor = encryptor,
            logger = getLogger(),
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

    /**
     * Documents a class, function, or property.
     *
     * This is a placeholder function intended to be documented. It currently does nothing and serves
     * only as an example for documentation generation.
     *
     * @param name A string parameter, its purpose is yet to be defined.
     * @return A string, the content of which is currently undefined.
     */
    fun document(name: String): Document {
        val dataStore = provideDocumentDataStoreInstance(name, Encryptor.None)
        return DataStoreDocument(dataStore)
    }

    /**
     * Creates or retrieves a named, encrypted [Document] instance using a custom [Encryptor].
     *
     * This function provides an implementation of the [Kvs] interface that
     * encrypts data before storing it, typically using DataStore as the backend.
     * Each unique [name] will correspond to a distinct, encrypted DataStore file.
     * This overload allows for providing a custom encryption and decryption logic.
     *
     * @param name The unique name for the encrypted KVS instance. This name is often
     *             used as the filename for the underlying DataStore.
     * @param encryptor The [Encryptor] implementation to be used for data encryption
     *                  and decryption.
     * @return An encrypted [Document] instance associated with the given [name] and [encryptor].
     */
    fun encryptDocument(name: String, secretKey: String): Document {
        val dataStore = provideDocumentDataStoreInstance(
            name = name,
            encryptor = encryptor(
                key = secretKey,
                logger = getLogger()
            )
        )
        return DataStoreDocument(dataStore)
    }

}