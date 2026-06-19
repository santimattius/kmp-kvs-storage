@file:OptIn(ExperimentalKvsTtl::class)

package com.santimattius.kvs

import com.santimattius.kvs.internal.OptimizedKvs
import com.santimattius.kvs.internal.OptimizedKvsExtended
import com.santimattius.kvs.internal.provideDatabase
import com.santimattius.kvs.ttl.Ttl
import com.santimattius.kvs.internal.ttl.TtlManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Returns a persistent [Kvs] using the **optimized** backend.
 *
 * Optimized persistence is designed for large key counts and efficient TTL cleanup.
 * Requires `kvs-persistence-optimized` on the classpath.
 *
 * When both light and optimized backends are present, call [kvsOptimized] explicitly
 * for optimized persistence or [kvsLight] (from `kvs-persistence-light`) for light persistence.
 */
fun Storage.kvsOptimized(name: String): Kvs =
    OptimizedKvs(provideDatabase(name).kvsEntryQueries)

/**
 * Returns a persistent [KvsExtended] with optional TTL using the **optimized** backend.
 */
@ExperimentalKvsTtl
fun Storage.kvsOptimized(name: String, ttl: Ttl? = null): KvsExtended =
    OptimizedKvsExtended(
        queries = provideDatabase(name).kvsEntryQueries,
        ttlManager = TtlManager(defaultTtl = ttl?.value()?.milliseconds)
    )

/**
 * Returns a persistent [KvsExtended] with optional TTL using the **optimized** backend.
 */
@ExperimentalKvsTtl
fun Storage.kvsOptimized(name: String, ttl: Duration? = null): KvsExtended =
    OptimizedKvsExtended(
        queries = provideDatabase(name).kvsEntryQueries,
        ttlManager = TtlManager(defaultTtl = ttl)
    )

@Deprecated("Use kvsOptimized", ReplaceWith("kvsOptimized(name)"))
fun Storage.optimizedKvs(name: String): Kvs = kvsOptimized(name)

@Deprecated("Use kvsOptimized with ttl parameter", ReplaceWith("kvsOptimized(name, ttl)"))
@ExperimentalKvsTtl
fun Storage.optimizedKvs(name: String, ttl: Ttl? = null): KvsExtended = kvsOptimized(name, ttl)

@Deprecated("Use kvsOptimized with ttl parameter", ReplaceWith("kvsOptimized(name, ttl)"))
@ExperimentalKvsTtl
fun Storage.optimizedKvs(name: String, ttl: Duration? = null): KvsExtended = kvsOptimized(name, ttl)
