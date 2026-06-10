@file:OptIn(com.santimattius.kvs.ExperimentalKvsTtl::class)

package com.santimattius.kvs.internal

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.santimattius.kvs.KvsExtended
import com.santimattius.kvs.internal.exception.WriteKvsException
import com.santimattius.kvs.internal.ttl.CleanupJob
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.persistence.optimized.db.KvsEntry
import com.santimattius.kvs.persistence.optimized.db.KvsEntryQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration

internal class OptimizedKvsExtended(
    internal val queries: KvsEntryQueries,
    private val ttlManager: TtlManager
) : KvsExtended {

    override suspend fun getAll(): Map<String, Any> = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        val all = queries.selectAll().executeAsList()
        val expired = all.filter { it.isExpired(now) }
        if (expired.isNotEmpty()) {
            queries.transaction { expired.forEach { queries.deleteByKey(it.key) } }
        }
        all.filter { !it.isExpired(now) }.associate { it.key to (it.value_ as Any) }
    }

    override suspend fun getString(key: String, defValue: String): String =
        withContext(Dispatchers.Default) { getOrDefault(key, defValue, String::toString) }

    override suspend fun getInt(key: String, defValue: Int): Int =
        withContext(Dispatchers.Default) { getOrDefault(key, defValue, String::toIntOrNull) }

    override suspend fun getLong(key: String, defValue: Long): Long =
        withContext(Dispatchers.Default) { getOrDefault(key, defValue, String::toLongOrNull) }

    override suspend fun getFloat(key: String, defValue: Float): Float =
        withContext(Dispatchers.Default) { getOrDefault(key, defValue, String::toFloatOrNull) }

    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean =
        withContext(Dispatchers.Default) { getOrDefault(key, defValue, String::toBoolean) }

    override fun getAllAsStream(): Flow<Map<String, Any>> =
        queries.selectAll().asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                val now = Clock.System.now().toEpochMilliseconds()
                list.filter { !it.isExpired(now) }.associate { it.key to (it.value_ as Any) }
            }

    override fun getStringAsStream(key: String, defValue: String): Flow<String> =
        queries.selectByKey(key).asFlow().mapToOneOrNull(Dispatchers.Default)
            .map { row -> if (row == null || row.isExpiredNow()) defValue else row.value_ }

    override fun getIntAsStream(key: String, defValue: Int): Flow<Int> =
        queries.selectByKey(key).asFlow().mapToOneOrNull(Dispatchers.Default)
            .map { row -> if (row == null || row.isExpiredNow()) defValue else row.value_.toIntOrNull() ?: defValue }

    override fun getLongAsStream(key: String, defValue: Long): Flow<Long> =
        queries.selectByKey(key).asFlow().mapToOneOrNull(Dispatchers.Default)
            .map { row -> if (row == null || row.isExpiredNow()) defValue else row.value_.toLongOrNull() ?: defValue }

    override fun getFloatAsStream(key: String, defValue: Float): Flow<Float> =
        queries.selectByKey(key).asFlow().mapToOneOrNull(Dispatchers.Default)
            .map { row -> if (row == null || row.isExpiredNow()) defValue else row.value_.toFloatOrNull() ?: defValue }

    override fun getBooleanAsStream(key: String, defValue: Boolean): Flow<Boolean> =
        queries.selectByKey(key).asFlow().mapToOneOrNull(Dispatchers.Default)
            .map { row -> if (row == null || row.isExpiredNow()) defValue else row.value_.toBoolean() }

    override fun edit(): KvsExtended.KvsExtendedEditor = OptimizedKvsExtendedEditor(queries, ttlManager)

    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.Default) {
        val row = queries.selectByKey(key).executeAsOneOrNull() ?: return@withContext false
        !row.isExpiredNow()
    }

    override fun cleanupJob(interval: Duration): CleanupJob = TtlBatchCleanupJob(queries, interval)

    private fun <T> getOrDefault(key: String, defValue: T, convert: (String) -> T?): T {
        val row = queries.selectByKey(key).executeAsOneOrNull() ?: return defValue
        if (row.isExpiredNow()) return defValue
        return convert(row.value_) ?: defValue
    }

    private fun KvsEntry.isExpired(nowMillis: Long): Boolean =
        expires_at != null && expires_at <= nowMillis

    private fun KvsEntry.isExpiredNow(): Boolean =
        isExpired(Clock.System.now().toEpochMilliseconds())
}

private class OptimizedKvsExtendedEditor(
    private val queries: KvsEntryQueries,
    private val ttlManager: TtlManager
) : KvsExtended.KvsExtendedEditor {

    private sealed interface Op {
        data class Put(val key: String, val value: String, val expiresAt: Long?) : Op
        data class Delete(val key: String) : Op
        data object Clear : Op
    }

    private val ops = mutableListOf<Op>()

    private fun put(key: String, value: String, duration: Duration? = null) =
        apply { ops += Op.Put(key, value, ttlManager.calculateExpiration(duration)) }

    override fun putString(key: String, value: String) = put(key, value)
    override fun putString(key: String, value: String, duration: Duration) = put(key, value, duration)
    override fun putInt(key: String, value: Int) = put(key, value.toString())
    override fun putInt(key: String, value: Int, duration: Duration) = put(key, value.toString(), duration)
    override fun putLong(key: String, value: Long) = put(key, value.toString())
    override fun putLong(key: String, value: Long, duration: Duration) = put(key, value.toString(), duration)
    override fun putFloat(key: String, value: Float) = put(key, value.toString())
    override fun putFloat(key: String, value: Float, duration: Duration) = put(key, value.toString(), duration)
    override fun putBoolean(key: String, value: Boolean) = put(key, value.toString())
    override fun putBoolean(key: String, value: Boolean, duration: Duration) = put(key, value.toString(), duration)
    override fun remove(key: String) = apply { ops += Op.Delete(key) }
    override fun clear() = apply { ops += Op.Clear }

    override suspend fun commit() = withContext(Dispatchers.Default) {
        try {
            queries.transaction {
                for (op in ops) when (op) {
                    is Op.Put -> queries.upsert(op.key, op.value, op.expiresAt)
                    is Op.Delete -> queries.deleteByKey(op.key)
                    Op.Clear -> queries.deleteAll()
                }
            }
        } catch (e: Exception) {
            throw WriteKvsException(e.message ?: "commit failed", e)
        }
    }
}
