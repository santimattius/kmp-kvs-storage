package com.santimattius.kvs.internal

import kotlinx.coroutines.flow.Flow

interface KvsStream {

    fun getAllAsStream(): Flow<Map<String, Any>>

    fun getStringAsStream(key: String, defValue: String): Flow<String>

    fun getIntAsStream(key: String, defValue: Int): Flow<Int>

    fun getLongAsStream(key: String, defValue: Long): Flow<Long>

    fun getFloatAsStream(key: String, defValue: Float): Flow<Float>

    fun getBooleanAsStream(key: String, defValue: Boolean): Flow<Boolean>
}
