@file:OptIn(ExperimentalKvsTtl::class)
@file:kotlin.jvm.JvmName("LightStorageExtensions")

package com.santimattius.kvs

import com.santimattius.kvs.internal.DataStoreKvs
import com.santimattius.kvs.internal.datastore.encrypt.DsEncryptStorage
import com.santimattius.kvs.Encryptor
import com.santimattius.kvs.internal.datastore.encrypt.encryptor
import com.santimattius.kvs.internal.datastore.storage.DsStorage
import com.santimattius.kvs.internal.logger.NoopKvsLogger
import com.santimattius.kvs.internal.logger.logger
import com.santimattius.kvs.internal.provideDataStoreInstance
import com.santimattius.kvs.ttl.Ttl
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.internal.ttl.extended.TtlKvsExtended
import com.santimattius.kvs.internal.ttl.provideTtlDataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Returns a persistent [Kvs] using the **light** backend.
 *
 * Light persistence is ideal for small to medium key counts and low binary overhead.
 * Requires `kvs-persistence-light` on the classpath.
 *
 * When both light and optimized backends are present, call [kvsLight] explicitly
 * for light persistence or [kvsOptimized] (from `kvs-persistence-optimized`) for
 * optimized persistence.
 */
fun Storage.kvsLight(name: String): Kvs = DataStoreKvs(
    dataStore = DsStorage(
        dataStore = provideDataStoreInstance(name),
        dispatcher = Dispatchers.IO
    )
)

/**
 * Returns an encrypted persistent [Kvs] using the **light** backend.
 * Requires `kvs-persistence-light` on the classpath.
 */
fun Storage.kvsLightEncrypt(name: String, secretKey: String): Kvs = kvsLightEncrypt(
    name = name,
    encryptor = encryptor(key = secretKey, logger = logger())
)

/**
 * Returns an encrypted persistent [Kvs] using the **light** backend and a custom [Encryptor].
 */
fun Storage.kvsLightEncrypt(name: String, encryptor: Encryptor): Kvs = DataStoreKvs(
    dataStore = DsEncryptStorage(
        dataStore = provideDataStoreInstance(name),
        encryptor = encryptor,
        logger = NoopKvsLogger,
        dispatcher = Dispatchers.IO
    )
)

/**
 * Returns a persistent [KvsExtended] with optional TTL and encryption using the **light** backend.
 * Requires `kvs-persistence-light` on the classpath.
 */
@ExperimentalKvsTtl
fun Storage.kvsLight(name: String, ttl: Ttl? = null, encrypted: Boolean = false): KvsExtended =
    kvsLight(name = name, ttl = ttl?.value()?.milliseconds, encrypted = encrypted)

/**
 * Returns a persistent [KvsExtended] with optional TTL and encryption using the **light** backend.
 */
@ExperimentalKvsTtl
fun Storage.kvsLight(name: String, ttl: Duration? = null, encrypted: Boolean = false): KvsExtended =
    TtlKvsExtended(
        dataStore = provideTtlDataStoreInstance(
            name = name,
            encryptor = if (encrypted) encryptor(key = name, logger = logger()) else Encryptor.None
        ),
        ttlManager = TtlManager(defaultTtl = ttl)
    )

@Deprecated("Use kvsLight", ReplaceWith("kvsLight(name)"))
fun Storage.simpleKvs(name: String): Kvs = kvsLight(name)

@Deprecated("Use kvsLight", ReplaceWith("kvsLight(name)"))
fun Storage.kvs(name: String): Kvs = kvsLight(name)

@Deprecated("Use kvsLightEncrypt", ReplaceWith("kvsLightEncrypt(name, secretKey)"))
fun Storage.simpleEncryptKvs(name: String, key: String): Kvs = kvsLightEncrypt(name, key)

@Deprecated("Use kvsLightEncrypt", ReplaceWith("kvsLightEncrypt(name, key)"))
fun Storage.encryptKvs(name: String, key: String): Kvs = kvsLightEncrypt(name, key)

@Deprecated("Use kvsLightEncrypt", ReplaceWith("kvsLightEncrypt(name, encryptor)"))
fun Storage.encryptKvs(name: String, encryptor: Encryptor): Kvs = kvsLightEncrypt(name, encryptor)

@Deprecated("Use kvsLight with ttl parameter", ReplaceWith("kvsLight(name, ttl, encrypted)"))
@ExperimentalKvsTtl
fun Storage.kvs(name: String, ttl: Ttl? = null, encrypted: Boolean = false): KvsExtended =
    kvsLight(name = name, ttl = ttl, encrypted = encrypted)

@Deprecated("Use kvsLight with ttl parameter", ReplaceWith("kvsLight(name, ttl, encrypted)"))
@ExperimentalKvsTtl
fun Storage.kvs(name: String, ttl: Duration? = null, encrypted: Boolean = false): KvsExtended =
    kvsLight(name = name, ttl = ttl, encrypted = encrypted)
