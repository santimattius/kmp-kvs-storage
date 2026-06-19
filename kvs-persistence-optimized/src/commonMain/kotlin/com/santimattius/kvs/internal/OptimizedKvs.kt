package com.santimattius.kvs.internal

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.santimattius.kvs.Kvs
import com.santimattius.kvs.internal.exception.WriteKvsException
import com.santimattius.kvs.persistence.optimized.db.KvsEntryQueries
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class OptimizedKvs(
    internal val queries: KvsEntryQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Kvs {

    override suspend fun getAll(): Map<String, Any> = withContext(dispatcher) {
        queries.selectAll().executeAsList().associate { it.key to (it.value_ as Any) }
    }

    override suspend fun getString(key: String, defValue: String): String =
        withContext(dispatcher) {
            queries.selectByKey(key).executeAsOneOrNull()?.value_ ?: defValue
        }

    override suspend fun getInt(key: String, defValue: Int): Int =
        withContext(dispatcher) {
            queries.selectByKey(key).executeAsOneOrNull()?.value_?.toIntOrNull() ?: defValue
        }

    override suspend fun getLong(key: String, defValue: Long): Long =
        withContext(dispatcher) {
            queries.selectByKey(key).executeAsOneOrNull()?.value_?.toLongOrNull() ?: defValue
        }

    override suspend fun getFloat(key: String, defValue: Float): Float =
        withContext(dispatcher) {
            queries.selectByKey(key).executeAsOneOrNull()?.value_?.toFloatOrNull() ?: defValue
        }

    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean =
        withContext(dispatcher) {
            queries.selectByKey(key).executeAsOneOrNull()?.value_?.toBoolean() ?: defValue
        }

    override fun getAllAsStream(): Flow<Map<String, Any>> =
        queries.selectAll().asFlow()
            .mapToList(dispatcher)
            .map { list -> list.associate { it.key to (it.value_ as Any) } }

    override fun getStringAsStream(key: String, defValue: String): Flow<String> =
        queries.selectByKey(key).asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.value_ ?: defValue }

    override fun getIntAsStream(key: String, defValue: Int): Flow<Int> =
        queries.selectByKey(key).asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.value_?.toIntOrNull() ?: defValue }

    override fun getLongAsStream(key: String, defValue: Long): Flow<Long> =
        queries.selectByKey(key).asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.value_?.toLongOrNull() ?: defValue }

    override fun getFloatAsStream(key: String, defValue: Float): Flow<Float> =
        queries.selectByKey(key).asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.value_?.toFloatOrNull() ?: defValue }

    override fun getBooleanAsStream(key: String, defValue: Boolean): Flow<Boolean> =
        queries.selectByKey(key).asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.value_?.toBoolean() ?: defValue }

    override fun edit(): Kvs.KvsEditor = OptimizedKvsEditor(queries)

    override suspend fun contains(key: String): Boolean = withContext(dispatcher) {
        queries.selectByKey(key).executeAsOneOrNull() != null
    }
}

private class OptimizedKvsEditor(
    private val queries: KvsEntryQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Kvs.KvsEditor {

    private sealed interface Op {
        data class Put(val key: String, val value: String) : Op
        data class Delete(val key: String) : Op
        data object Clear : Op
    }

    private val ops = mutableListOf<Op>()

    override fun putString(key: String, value: String) = apply { ops += Op.Put(key, value) }
    override fun putInt(key: String, value: Int) = apply { ops += Op.Put(key, value.toString()) }
    override fun putLong(key: String, value: Long) = apply { ops += Op.Put(key, value.toString()) }
    override fun putFloat(key: String, value: Float) =
        apply { ops += Op.Put(key, value.toString()) }

    override fun putBoolean(key: String, value: Boolean) =
        apply { ops += Op.Put(key, value.toString()) }

    override fun remove(key: String) = apply { ops += Op.Delete(key) }
    override fun clear() = apply { ops += Op.Clear }

    override suspend fun commit() = withContext(dispatcher) {
        try {
            queries.transaction {
                for (op in ops) when (op) {
                    is Op.Put -> queries.upsert(op.key, op.value, null)
                    is Op.Delete -> queries.deleteByKey(op.key)
                    Op.Clear -> queries.deleteAll()
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw WriteKvsException(e.message ?: "commit failed", e)
        }
    }
}
