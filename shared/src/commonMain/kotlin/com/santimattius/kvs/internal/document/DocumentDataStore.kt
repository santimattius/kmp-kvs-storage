package com.santimattius.kvs.internal.document

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import com.santimattius.kvs.internal.datastore.encrypt.Encryptor
import com.santimattius.kvs.internal.producePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import okio.Path.Companion.toPath
import okio.use

internal fun dataStorage(
    name: String,
    encryptor: Encryptor
): DataStore<String> {
    return DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = platformFileSystem(),
            serializer = StringSerializer(encryptor),
            producePath = { producePath(name).toPath() }),
    )
}

private class StringSerializer(
    private val encryptor: Encryptor
) : OkioSerializer<String> {

    override val defaultValue: String = ""

    override suspend fun readFrom(source: BufferedSource): String {
        val encryptedBytes = withContext(Dispatchers.IO) {
            source.use { it.readByteArray() }
        }
        val decryptedBytes = encryptor.decrypt(encryptedBytes.decodeToString())
        return decryptedBytes
    }

    override suspend fun writeTo(t: String, sink: BufferedSink) {
        val encrypt = encryptor.encrypt(t)
        withContext(Dispatchers.IO) {
            sink.use {
                it.write(encrypt.encodeToByteArray())
            }
        }
    }
}