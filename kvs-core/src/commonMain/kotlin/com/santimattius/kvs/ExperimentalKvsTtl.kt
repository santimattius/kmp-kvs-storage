package com.santimattius.kvs

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is an experimental KVS TTL API. It may change in future releases."
)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalKvsTtl
