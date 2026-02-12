package com.santimattius.kvs

import kotlin.RequiresOptIn

/**
 * Marks the TTL (Time-To-Live) API as experimental.
 *
 * The TTL feature may change in future releases. Use with awareness that the API
 * or behavior could change without notice. Feedback is welcome.
 *
 * To use TTL APIs, opt in with [OptIn]:
 * ```
 * @OptIn(ExperimentalKvsTtl::class)
 * fun useTtlCache() {
 *     val kvs = Storage.kvs("cache", ttl = myTtl)
 * }
 * ```
 *
 * Or opt in for an entire file:
 * ```
 * @file:OptIn(ExperimentalKvsTtl::class)
 * package my.app
 * ```
 */
@RequiresOptIn(
    message = "TTL (Time-To-Live) is experimental. The API may change in future releases.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS
)
annotation class ExperimentalKvsTtl
