package com.santimattius.kvs.internal.logger

internal const val TAG = "KvsStorage"

/**
 * Returns a platform-specific logger.
 * @return A [KvsLogger] instance.
 */
internal expect fun logger(): KvsLogger