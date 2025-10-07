package com.santimattius.kvs.internal.extensions

import kotlinx.coroutines.CancellationException

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 * This function is similar to the standard library's `runCatching`, but it specifically re-throws [CancellationException]
 * to ensure that coroutine cancellation is not accidentally caught and suppressed.
 *
 * This is crucial in asynchronous code where `runCatching` might otherwise hide a cancellation signal,
 * leading to unresponsive or resource-leaking coroutines.
 *
 * @param T The receiver type of the function block.
 * @param R The return type of the function block.
 * @param block A function block to be executed on the receiver `T`.
 * @return An instance of [Result] encapsulating the successful result or the caught [Throwable] exception.
 */
inline fun <T, R> T.runNonCancellableCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}