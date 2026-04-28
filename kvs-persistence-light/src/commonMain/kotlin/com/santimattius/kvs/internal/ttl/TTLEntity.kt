package com.santimattius.kvs.internal.ttl

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
internal data class TTLEntity(
    val key: String,
    val value: String,
    val duration: Duration? = null,
    val expiresAt: Long? = null,
    val encrypted: Boolean = false
)
