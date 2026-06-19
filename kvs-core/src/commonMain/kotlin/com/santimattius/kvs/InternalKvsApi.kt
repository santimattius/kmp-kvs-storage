package com.santimattius.kvs

/** Internal API — not stable for external use. Subject to change without notice. */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal KVS API not intended for external use. It may change or be removed without notice."
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY
)
annotation class InternalKvsApi
