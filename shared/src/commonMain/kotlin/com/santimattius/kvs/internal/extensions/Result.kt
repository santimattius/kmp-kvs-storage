package com.santimattius.kvs.internal.extensions

import kotlinx.coroutines.CancellationException

inline fun <T, R> T.runNonCancellableCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}