package com.santimattius.kvs

import com.santimattius.kvs.internal.extensions.runNonCancellableCatching
import kotlinx.coroutines.CancellationException
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC


/**
 * Retrieves a string value from the Kvs, returning it as a [Result].
 *
 * This function wraps the [Kvs.getString] call in a [runNonCancellableCatching] block,
 * ensuring that any exceptions thrown during the retrieval are caught and returned
 * as a failure [Result].
 *
 * @param key The key of the string value to retrieve.
 * @param defValue The default value to return if the key is not found or an error occurs.
 * @return A [Result] containing the string value if successful, or an exception if an error occurred.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getStringAsResult(key: String, defValue: String): Result<String> {
    return runNonCancellableCatching {
        getString(key, defValue)
    }
}

/**
 * Retrieves an integer value from the Kvs store for the given [key].
 *
 * This function is a suspend function, meaning it can be paused and resumed later,
 * typically for asynchronous operations like I/O.
 *
 * It uses [runNonCancellableCatching] to ensure that the operation completes even if
 * the coroutine is cancelled, and it wraps the result in a [Result] type.
 *
 * @param key The key whose associated value is to be returned.
 * @param defValue The default value to return if the key is not found or an error occurs.
 * @return A [Result] containing the integer value if successful, or an exception if an error occurred.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getIntAsResult(key: String, defValue: Int): Result<Int> {
    return runNonCancellableCatching {
        getInt(key, defValue)
    }
}

/**
 * Retrieves a Long value associated with the given [key] from the Kvs store.
 *
 * This function is a suspend function, meaning it can be called from a coroutine.
 * It uses `runNonCancellableCatching` to ensure that the operation is not cancelled
 * even if the coroutine is cancelled. This is important for operations that should
 * complete regardless of cancellation, such as saving data.
 *
 * @param key The key whose associated value is to be returned.
 * @param defValue The default value to return if the key is not found or an error occurs.
 * @return A [Result] object containing the Long value if successful, or an exception if an error occurred.
 *         If the key is not found, the [defValue] is returned within the [Result].
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getLongAsResult(key: String, defValue: Long): Result<Long> {
    return runNonCancellableCatching {
        getLong(key, defValue)
    }
}

/**
 * Retrieves a float value associated with the given key from the Key-Value Store (KVS).
 * If the key is not found, it returns the provided default value.
 * This operation is performed non-cancellably and returns a [Result] object,
 * encapsulating either the successfully retrieved float or an exception if an error occurred.
 *
 * @param key The key whose associated float value is to be returned.
 * @param defValue The default float value to return if the key is not found.
 * @return A [Result] containing the float value if found, or the [defValue] if not found,
 *         or an exception if an error occurs during the retrieval process.
 */
suspend fun Kvs.getFloatAsResult(key: String, defValue: Float): Result<Float> {
    return runNonCancellableCatching {
        getFloat(key, defValue)
    }
}

/**
 * Retrieves a boolean value from the KVS (Key-Value Store) for the given key.
 *
 * This function is a suspend function, meaning it can perform asynchronous operations without blocking the thread.
 * It uses `runNonCancellableCatching` to ensure that the operation is not cancelled and to catch any exceptions
 * that might occur during the retrieval process.
 *
 * @param key The key whose associated boolean value is to be returned.
 * @param defValue The default boolean value to return if the key is not found or an error occurs.
 * @return A [Result] object containing the boolean value if successful, or an exception if an error occurred.
 *         The boolean value will be the one associated with the key, or `defValue` if the key is not found.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getBooleanAsResult(key: String, defValue: Boolean): Result<Boolean> {
    return runNonCancellableCatching {
        getBoolean(key, defValue)
    }
}

/**
 * Retrieves all key-value pairs from the Kvs store as a [Result].
 *
 * This function wraps the [Kvs.getAll] call in a [runNonCancellableCatching] block,
 * ensuring that any exceptions thrown during the operation are caught and returned
 * as a [Result.Failure]. If the operation is successful, it returns a [Result.Success]
 * containing a [Map] of all key-value pairs.
 *
 * @return A [Result] containing a [Map] of all key-value pairs if successful,
 *         or a [Result.Failure] with the exception if an error occurs.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getAllAsResult(): Result<Map<String, Any>> {
    return runNonCancellableCatching {
        getAll()
    }
}

/**
 * Asynchronously applies the changes made in this editor to the Kvs.
 *
 * This function attempts to commit the changes and returns a [Result] indicating success or failure.
 * The operation is executed in a non-cancellable context to ensure that the commit either completes
 * or fails atomically.
 *
 * @return A [Result] containing `true` if the commit was successful, or an exception if it failed.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.KvsEditor.apply(): Result<Boolean> {
    return runNonCancellableCatching {
        commit()
        true
    }
}