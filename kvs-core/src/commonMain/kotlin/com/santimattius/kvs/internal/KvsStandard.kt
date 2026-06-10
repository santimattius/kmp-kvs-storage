package com.santimattius.kvs.internal

import com.santimattius.kvs.internal.exception.GetAllKvsException
import com.santimattius.kvs.internal.exception.KvsException
import com.santimattius.kvs.internal.exception.ReadKvsException
import kotlinx.coroutines.CancellationException

interface KvsStandard {

    @Throws(GetAllKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getAll(): Map<String, Any>

    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getString(key: String, defValue: String): String

    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getInt(key: String, defValue: Int): Int

    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getLong(key: String, defValue: Long): Long

    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getFloat(key: String, defValue: Float): Float

    @Throws(ReadKvsException::class, KvsException::class, CancellationException::class)
    suspend fun getBoolean(key: String, defValue: Boolean): Boolean
}
