package com.santimattius.kvs.internal.ttl

import kotlin.time.Clock
import kotlin.time.Duration

/**
 * Manages Time-To-Live (TTL) calculations and expiration checks for key-value storage.
 *
 * This class provides functionality to calculate expiration timestamps and verify if entities
 * have expired based on their expiration timestamps. It supports a default TTL that can be
 * applied when no specific TTL is provided for individual keys.
 *
 * @property defaultTtl The default TTL duration to use when no specific TTL is provided.
 *                      If `null`, keys without explicit TTL will not expire.
 * @property clock The clock instance used for time calculations. Defaults to [Clock.System].
 *
 * @sample
 * ```
 * val manager = TtlManager(defaultTtl = Duration.ofHours(1))
 * val expiresAt = manager.calculateExpiration(null) // Uses defaultTtl
 * val isExpired = manager.isExpired(expiresAt)
 * ```
 */
internal class TtlManager(
    val defaultTtl: Duration? = null,
    private val clock: Clock = Clock.System
) {

    /**
     * Calculates the expiration timestamp for a given TTL duration.
     *
     * If [ttl] is `null`, the [defaultTtl] will be used. If both are `null`,
     * the result will be `null` (indicating no expiration).
     *
     * @param ttl The TTL duration. If `null`, [defaultTtl] will be used.
     * @return The expiration timestamp in milliseconds since epoch, or `null` if no TTL is configured.
     */
    fun calculateExpiration(ttl: Duration?): Long? {
        val effectiveTtl = ttl ?: defaultTtl
        return effectiveTtl?.let {
            clock.now().toEpochMilliseconds() + it.inWholeMilliseconds
        }
    }

    /**
     * Checks if an entity has expired based on its expiration timestamp.
     *
     * @param expiresAt The expiration timestamp in milliseconds since epoch.
     *                  If `null`, the entity is considered not expired.
     * @return `true` if the entity has expired (current time >= expiresAt), `false` otherwise.
     */
    fun isExpired(expiresAt: Long?): Boolean {
        return expiresAt != null && clock.now().toEpochMilliseconds() >= expiresAt
    }
}