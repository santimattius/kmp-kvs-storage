package com.santimattius.kvs.internal.ttl

/**
 * Represents a Time-To-Live (TTL) configuration.
 *
 * This interface provides a way to specify TTL values for key-value storage.
 * Implementations should return the TTL duration in milliseconds.
 *
 * @sample
 * ```
 * val ttl = object : Ttl {
 *     override fun value() = Duration.ofHours(1).inWholeMilliseconds
 * }
 * ```
 */
interface Ttl {

    /**
     * Returns the TTL duration in milliseconds.
     *
     * @return The TTL duration in milliseconds.
     */
    fun value(): Long
}