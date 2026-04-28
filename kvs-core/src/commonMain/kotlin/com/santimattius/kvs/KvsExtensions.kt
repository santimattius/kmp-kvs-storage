package com.santimattius.kvs

import com.santimattius.kvs.internal.extensions.runNonCancellableCatching
import kotlinx.coroutines.CancellationException
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getStringAsResult(key: String, defValue: String): Result<String> =
    runNonCancellableCatching { getString(key, defValue) }

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getIntAsResult(key: String, defValue: Int): Result<Int> =
    runNonCancellableCatching { getInt(key, defValue) }

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getLongAsResult(key: String, defValue: Long): Result<Long> =
    runNonCancellableCatching { getLong(key, defValue) }

suspend fun Kvs.getFloatAsResult(key: String, defValue: Float): Result<Float> =
    runNonCancellableCatching { getFloat(key, defValue) }

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getBooleanAsResult(key: String, defValue: Boolean): Result<Boolean> =
    runNonCancellableCatching { getBoolean(key, defValue) }

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.getAllAsResult(): Result<Map<String, Any>> =
    runNonCancellableCatching { getAll() }

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Throws(CancellationException::class)
suspend fun Kvs.KvsEditor.apply(): Result<Boolean> =
    runNonCancellableCatching { commit(); true }
