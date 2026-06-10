package com.santimattius.kvs.internal.memory

import com.santimattius.kvs.internal.KvsStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class InMemoryKvsStream(private val preferences: InMemoryPreferences) : KvsStream {

    override fun getAllAsStream(): Flow<Map<String, Any>> = preferences.stream

    override fun getStringAsStream(key: String, defValue: String): Flow<String> =
        preferences.stream.map { it[key] as? String ?: defValue }

    override fun getIntAsStream(key: String, defValue: Int): Flow<Int> =
        preferences.stream.map { it[key] as? Int ?: defValue }

    override fun getLongAsStream(key: String, defValue: Long): Flow<Long> =
        preferences.stream.map { it[key] as? Long ?: defValue }

    override fun getFloatAsStream(key: String, defValue: Float): Flow<Float> =
        preferences.stream.map { it[key] as? Float ?: defValue }

    override fun getBooleanAsStream(key: String, defValue: Boolean): Flow<Boolean> =
        preferences.stream.map { it[key] as? Boolean ?: defValue }
}
