@file:OptIn(com.santimattius.kvs.ExperimentalKvsTtl::class)

package com.santimattius.kvs

import com.santimattius.kvs.internal.OptimizedKvs
import com.santimattius.kvs.internal.OptimizedKvsExtended
import com.santimattius.kvs.internal.provideDatabase
import com.santimattius.kvs.internal.ttl.Ttl
import com.santimattius.kvs.internal.ttl.TtlManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun Storage.optimizedKvs(name: String): Kvs =
    OptimizedKvs(provideDatabase(name).kvsEntryQueries)

@ExperimentalKvsTtl
fun Storage.optimizedKvs(name: String, ttl: Ttl? = null): KvsExtended =
    OptimizedKvsExtended(
        queries = provideDatabase(name).kvsEntryQueries,
        ttlManager = TtlManager(defaultTtl = ttl?.value()?.milliseconds)
    )

@ExperimentalKvsTtl
fun Storage.optimizedKvs(name: String, ttl: Duration? = null): KvsExtended =
    OptimizedKvsExtended(
        queries = provideDatabase(name).kvsEntryQueries,
        ttlManager = TtlManager(defaultTtl = ttl)
    )
