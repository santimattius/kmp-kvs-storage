package com.santimattius.kvs.internal.ttl

import com.santimattius.kvs.ExperimentalKvsTtl

/**
 * Represents a Time-To-Live (TTL) configuration.
 *
 * This interface provides a way to specify TTL values for key-value storage.
 * Implementations should return the TTL duration in milliseconds.
 *
 * **Experimental:** Part of the experimental TTL API. Opt in with `@OptIn(ExperimentalKvsTtl::class)`.
 *
 * @sample
 * ```
 * val ttl = object : Ttl {
 *     override fun value() = Duration.ofHours(1).inWholeMilliseconds
 * }
 * ```
 */
@ExperimentalKvsTtl
interface Ttl {

    /**
     * Returns the TTL duration in milliseconds.
     *
     * @return The TTL duration in milliseconds.
     */
    fun value(): Long
}