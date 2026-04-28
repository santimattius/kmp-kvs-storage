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

internal fun createTllDataStorage(
    name: String,
    encryptor: Encryptor
): DataStore<Map<String, TTLEntity>> {
    return DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = platformFileSystem(),
            serializer = TTLEntitySerializer(encryptor),
            producePath = { producePath(name).toPath() }
        )
    )
}

private class TTLEntitySerializer(
    private val encryptor: Encryptor
) : OkioSerializer<Map<String, TTLEntity>> {

    override val defaultValue: Map<String, TTLEntity> = emptyMap()

    override suspend fun readFrom(source: BufferedSource): Map<String, TTLEntity> {
        val encryptedBytes = withContext(Dispatchers.IO) {
            source.use { it.readByteArray() }
        }
        val decryptedBytes = encryptor.decrypt(encryptedBytes.decodeToString())
        return Json.decodeFromString<Map<String, TTLEntity>>(decryptedBytes)
    }

    override suspend fun writeTo(t: Map<String, TTLEntity>, sink: BufferedSink) {
        val encrypt = encryptor.encrypt(Json.encodeToString(t))
        withContext(Dispatchers.IO) {
            sink.use { it.write(encrypt.encodeToByteArray()) }
        }
    }
}
