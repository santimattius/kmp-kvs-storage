package com.santimattius.kvs.internal

import com.santimattius.kvs.GetAllKvsException
import com.santimattius.kvs.InternalKvsApi
import com.santimattius.kvs.KvsException
import com.santimattius.kvs.ReadKvsException
import kotlinx.coroutines.CancellationException

@InternalKvsApi
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
