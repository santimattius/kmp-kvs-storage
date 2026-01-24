package com.santimattius.kvs.internal.ttl

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import com.santimattius.kvs.internal.datastore.encrypt.Encryptor
import com.santimattius.kvs.internal.document.platformFileSystem
import com.santimattius.kvs.internal.producePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.Path.Companion.toPath
import okio.use

/**
 * Creates a new [DataStore] instance for storing TTL-enabled entities.
 *
 * This function creates a DataStore that stores a map of [TTLEntity] objects, with support
 * for encryption. The data is serialized as JSON and persisted using Okio file system.
 *
 * @param name The name of the DataStore, used to generate the file path.
 * @param encryptor The [Encryptor] instance to use for encryption/decryption of stored data.
 * @return A new [DataStore] instance configured for TTL entities.
 */
internal fun createTllDataStorage(
    name: String,
    encryptor: Encryptor
): DataStore<Map<String, TTLEntity>> {
    return DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = platformFileSystem(),
            serializer = TTLEntitySerializer(encryptor),
            producePath = { producePath(name).toPath() }),
    )
}

/**
 * Serializer for [TTLEntity] maps using JSON format with encryption support.
 *
 * This serializer handles reading and writing [Map] of [TTLEntity] objects to/from
 * a [BufferedSource]/[BufferedSink] with optional encryption.
 *
 * @property encryptor The [Encryptor] instance used for encryption/decryption.
 */
private class TTLEntitySerializer(
    private val encryptor: Encryptor
) : OkioSerializer<Map<String, TTLEntity>> {

    override val defaultValue: Map<String, TTLEntity> = emptyMap()

    /**
     * Reads and deserializes a map of [TTLEntity] from the source.
     *
     * The data is expected to be encrypted JSON that will be decrypted before deserialization.
     *
     * @param source The [BufferedSource] to read from.
     * @return A map of [TTLEntity] objects.
     */
    override suspend fun readFrom(source: BufferedSource): Map<String, TTLEntity> {
        val encryptedBytes = withContext(Dispatchers.IO) {
            source.use { it.readByteArray() }
        }
        //TODO: decrypt all items?
        val decryptedBytes = encryptor.decrypt(encryptedBytes.decodeToString())
        return Json.decodeFromString<Map<String, TTLEntity>>(decryptedBytes)
    }

    /**
     * Serializes and writes a map of [TTLEntity] to the sink.
     *
     * The data is serialized to JSON and then encrypted before being written.
     *
     * @param t The map of [TTLEntity] objects to write.
     * @param sink The [BufferedSink] to write to.
     */
    override suspend fun writeTo(t: Map<String, TTLEntity>, sink: BufferedSink) {
        //TODO: encrypt all items?
        val encrypt = encryptor.encrypt(Json.encodeToString(t))
        withContext(Dispatchers.IO) {
            sink.use {
                it.write(encrypt.encodeToByteArray())
            }
        }
    }
}