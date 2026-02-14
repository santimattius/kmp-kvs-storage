package com.santimattius.kvs.internal.ttl

import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * Represents a key-value entity with Time-To-Live (TTL) support.
 *
 * This data class encapsulates a stored value along with its TTL metadata, including
 * the original duration, calculated expiration timestamp, and encryption status.
 *
 * @property key The unique identifier for this entity.
 * @property value The stored value as a String (all types are serialized to String).
 * @property duration The original TTL duration specified when the entity was created.
 *                    This is optional and may be `null` if using a default TTL.
 * @property expiresAt The calculated expiration timestamp in milliseconds since epoch.
 *                     This is computed from [duration] or the default TTL at creation time.
 *                     If `null`, the entity never expires.
 * @property encrypted Indicates whether the entity value is encrypted. Defaults to `false`.
 *
 * @sample
 * ```
 * val entity = TTLEntity(
 *     key = "user_token",
 *     value = "abc123",
 *     duration = Duration.ofHours(1),
 *     expiresAt = 1737744000000L
 * )
 * ```
 */
@Serializable
data class TTLEntity(
    val key: String,
    val value: String,
    val duration: Duration? = null,
    val expiresAt: Long? = null,
    val encrypted: Boolean = false
)
