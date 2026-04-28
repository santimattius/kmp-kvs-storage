@file:OptIn(ExperimentalKvsTtl::class)
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
import com.santimattius.kvs.internal.ttl.Ttl
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.internal.ttl.extended.TtlKvsExtended
import com.santimattius.kvs.internal.ttl.provideTtlDataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun Storage.simpleKvs(name: String): Kvs = DataStoreKvs(
    dataStore = DsStorage(
        dataStore = provideDataStoreInstance(name),
        dispatcher = Dispatchers.IO
    )
)

@Deprecated("use simpleKvs", ReplaceWith("simpleKvs(name)"))
fun Storage.kvs(name: String): Kvs = simpleKvs(name)

fun Storage.simpleEncryptKvs(name: String, key: String): Kvs = encryptKvs(
    name = name,
    encryptor = encryptor(key = key, logger = logger())
)

@Deprecated("use simpleEncryptKvs", ReplaceWith("simpleEncryptKvs(name, key)"))
fun Storage.encryptKvs(name: String, key: String): Kvs = simpleEncryptKvs(name, key)

fun Storage.encryptKvs(name: String, encryptor: Encryptor): Kvs = DataStoreKvs(
    dataStore = DsEncryptStorage(
        dataStore = provideDataStoreInstance(name),
        encryptor = encryptor,
        logger = NoopKvsLogger,
        dispatcher = Dispatchers.IO
    )
)

@ExperimentalKvsTtl
fun Storage.kvs(name: String, ttl: Ttl? = null, encrypted: Boolean = false): KvsExtended =
    kvs(name = name, ttl = ttl?.value()?.milliseconds, encrypted = encrypted)

@ExperimentalKvsTtl
fun Storage.kvs(name: String, ttl: Duration? = null, encrypted: Boolean = false): KvsExtended =
    TtlKvsExtended(
        dataStore = provideTtlDataStoreInstance(
            name = name,
            encryptor = if (encrypted) encryptor(key = name, logger = logger()) else Encryptor.None
        ),
        ttlManager = TtlManager(defaultTtl = ttl)
    )

fun Storage.document(name: String): Document =
    DataStoreDocument(provideDocumentDataStoreInstance(name, Encryptor.None))

fun Storage.encryptDocument(name: String, secretKey: String): Document =
    DataStoreDocument(provideDocumentDataStoreInstance(name, encryptor(key = secretKey, logger = logger())))
