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
import com.santimattius.kvs.internal.ttl.Ttl
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.internal.ttl.extended.TtlKvsExtended
import com.santimattius.kvs.internal.ttl.provideTtlDataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.time.Duration.Companion.milliseconds

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
     * @deprecated Use [simpleKvs] instead.
     */
    @Deprecated("use simpleKvs", ReplaceWith("simpleKvs(name)"))
    fun kvs(name: String): Kvs = simpleKvs(name)

    /**
     * Creates or retrieves a named [Kvs] instance without TTL support.
     *
     * This function provides a standard implementation of the [Kvs] interface,
     * backed by DataStore. Keys stored in this instance will not expire.
     * Each unique [name] will correspond to a distinct DataStore file.
     *
     * @param name The unique name for the KVS instance. This name is often
     *             used as the filename for the underlying DataStore.
     * @return A [Kvs] instance associated with the given [name].
     *
     * @sample
     * ```
     * val kvs = Storage.simpleKvs("preferences")
     * kvs.edit().putString("key", "value").commit()
     * ```
     */
    fun simpleKvs(name: String): Kvs = DataStoreKvs(
        dataStore = DsStorage(
            dataStore = provideDataStoreInstance(name),
            dispatcher = Dispatchers.IO
        )
    )

    /**
     * Creates or retrieves a named [KvsExtended] instance with Time-To-Live (TTL) support.
     *
     * This function provides an implementation of the [KvsExtended] interface with automatic
     * expiration of stored keys based on TTL configuration. Keys can have individual TTL values
     * or use a default TTL configured at the instance level.
     *
     * **TTL Features:**
     * - Default TTL: Configured via [ttl] parameter, applies to all keys without explicit TTL
     * - Per-key TTL: Override default TTL for specific keys when storing values
     * - Automatic expiration: Expired keys are automatically filtered and cleaned up
     * - Lazy cleanup: Keys are removed when accessed or via background cleanup job
     *
     * **Encryption:**
     * When [encrypted] is `true`, the instance name is used as the encryption key.
     * For custom encryption, use the overload with an [Encryptor] parameter.
     *
     * @param name The unique name for the KVS instance. This name is used as the filename
     *             for the underlying DataStore and as the encryption key if [encrypted] is `true`.
     * @param ttl Optional default TTL for all keys. If `null`, keys without explicit TTL
     *            will not expire. If provided, all keys will use this TTL unless overridden.
     * @param encrypted If `true`, data will be encrypted using the instance name as the key.
     *                 Defaults to `false`.
     * @return A [KvsExtended] instance with TTL support associated with the given [name].
     *
     * @sample
     * ```
     * // Create KVS with default TTL of 1 hour
     * val ttl = object : Ttl {
     *     override fun value() = Duration.ofHours(1).inWholeMilliseconds
     * }
     * val cache = Storage.kvs("cache", ttl = ttl)
     *
     * // Store with default TTL
     * cache.edit().putString("key1", "value1").commit()
     *
     * // Override TTL for specific key
     * cache.edit().putString("key2", "value2", Duration.ofMinutes(30)).commit()
     *
     * // Encrypted TTL-enabled storage
     * val secureCache = Storage.kvs("secure", ttl = ttl, encrypted = true)
     * ```
     */
    fun kvs(name: String, ttl: Ttl? = null, encrypted: Boolean = false): KvsExtended =
        TtlKvsExtended(
            dataStore = provideTtlDataStoreInstance(
                name = name,
                encryptor = if (encrypted) encryptor(
                    key = name,
                    logger = getLogger()
                ) else Encryptor.None
            ),
            ttlManager = TtlManager(defaultTtl = ttl?.value()?.milliseconds)
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
     * @deprecated Use [simpleEncryptKvs] instead.
     */
    @Deprecated("use simpleEncryptKvs", ReplaceWith("simpleEncryptKvs(name, key)"))
    fun encryptKvs(name: String, key: String): Kvs {
        return simpleEncryptKvs(name, key)
    }

    /**
     * Creates or retrieves a named, encrypted [Kvs] instance without TTL support.
     *
     * This function provides an encrypted implementation of the [Kvs] interface,
     * backed by DataStore. Data is encrypted before storage and decrypted on retrieval.
     * Keys stored in this instance will not expire.
     *
     * @param name The unique name for the encrypted KVS instance. This name is often
     *             used as the filename for the underlying DataStore.
     * @param key The encryption key used to secure the data. It is crucial
     *            to manage this key securely.
     * @return An encrypted [Kvs] instance associated with the given [name] and [key].
     *
     * @sample
     * ```
     * val secureKvs = Storage.simpleEncryptKvs("secure_prefs", "my_secret_key")
     * secureKvs.edit().putString("sensitive", "data").commit()
     * ```
     */
    fun simpleEncryptKvs(name: String, key: String): Kvs {
        return encryptKvs(
            name = name,
            encryptor = encryptor(
                key = key,
                logger = getLogger()
            )
        )
    }

    /**
     * Creates or retrieves a named, encrypted [Kvs] instance with custom encryption.
     *
     * This function provides an implementation of the [Kvs] interface that
     * encrypts data before storing it using a custom [Encryptor] implementation.
     * Each unique [name] will correspond to a distinct, encrypted DataStore file.
     * Keys stored in this instance will not expire.
     *
     * @param name The unique name for the encrypted KVS instance. This name is often
     *             used as the filename for the underlying DataStore.
     * @param encryptor The [Encryptor] implementation to be used for data encryption
     *                  and decryption.
     * @return An encrypted [Kvs] instance associated with the given [name] and [encryptor].
     *
     * @sample
     * ```
     * val customEncryptor = MyCustomEncryptor()
     * val kvs = Storage.encryptKvs("custom_secure", customEncryptor)
     * ```
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
     * Creates or retrieves a named [Document] instance for single-object storage.
     *
     * This function provides a [Document] interface for storing and retrieving a single
     * serializable object (e.g., a user profile, configuration object). The document
     * is stored as a single entity in DataStore.
     *
     * **Note:** This is different from [Kvs] which stores multiple key-value pairs.
     * A [Document] stores a single object that can be retrieved as a whole.
     *
     * @param name The unique name for the Document instance. This name is often
     *             used as the filename for the underlying DataStore.
     * @return A [Document] instance associated with the given [name].
     *
     * @sample
     * ```
     * @Serializable
     * data class UserProfile(val name: String, val email: String)
     *
     * val document = Storage.document("user_profile")
     * document.put(UserProfile("John", "john@example.com"))
     * val profile: UserProfile? = document.get()
     * ```
     */
    fun document(name: String): Document {
        val dataStore = provideDocumentDataStoreInstance(name, Encryptor.None)
        return DataStoreDocument(dataStore)
    }

    /**
     * Creates or retrieves a named, encrypted [Document] instance.
     *
     * This function provides a [Document] interface for storing and retrieving a single
     * serializable object with encryption. The document is encrypted before storage and
     * decrypted on retrieval.
     *
     * **Note:** This is different from [Kvs] which stores multiple key-value pairs.
     * A [Document] stores a single object that can be retrieved as a whole.
     *
     * @param name The unique name for the encrypted Document instance. This name is often
     *             used as the filename for the underlying DataStore.
     * @param secretKey The encryption key used to secure the document data. It is crucial
     *                  to manage this key securely.
     * @return An encrypted [Document] instance associated with the given [name] and [secretKey].
     *
     * @sample
     * ```
     * @Serializable
     * data class SecureData(val token: String)
     *
     * val document = Storage.encryptDocument("secure_data", "my_secret_key")
     * document.put(SecureData("sensitive_token"))
     * val data: SecureData? = document.get()
     * ```
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