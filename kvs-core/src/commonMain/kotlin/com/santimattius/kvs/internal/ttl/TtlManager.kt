package com.santimattius.kvs.internal.ttl

import kotlin.time.Clock
import kotlin.time.Duration

class TtlManager(
    val defaultTtl: Duration? = null,
    private val clock: Clock = Clock.System,
) {
    fun calculateExpiration(ttl: Duration?): Long? {
        val effectiveTtl = ttl ?: defaultTtl
        return effectiveTtl?.let {
            clock.now().toEpochMilliseconds() + it.inWholeMilliseconds
        }
    }

    fun isExpired(expiresAt: Long?): Boolean =
        expiresAt != null && clock.now().toEpochMilliseconds() >= expiresAt
}
