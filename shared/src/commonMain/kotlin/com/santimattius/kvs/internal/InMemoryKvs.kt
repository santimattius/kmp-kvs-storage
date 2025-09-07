package com.santimattius.kvs.internal

import com.santimattius.kvs.Kvs
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal class InMemoryKvs : Kvs {

    private val lock = SynchronizedObject()

    private val data: AtomicReference<Map<String, Any>> = AtomicReference(emptyMap())

    private val _map: Map<String, Any>
        get() = data.load()

    override suspend fun getAll(): Map<String, Any> = synchronized(lock) {
        _map
    }

    override suspend fun getString(key: String, defValue: String): String = synchronized(lock) {
        _map[key] as? String ?: defValue
    }

    override suspend fun getInt(key: String, defValue: Int): Int = synchronized(lock) {
        _map[key] as? Int ?: defValue
    }

    override suspend fun getLong(key: String, defValue: Long): Long = synchronized(lock) {
        _map[key] as? Long ?: defValue
    }

    override suspend fun getFloat(key: String, defValue: Float): Float = synchronized(lock) {
        _map[key] as? Float ?: defValue
    }

    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean = synchronized(lock) {
        _map[key] as? Boolean ?: defValue
    }

    override fun edit(): Kvs.KvsEditor = InMemoryKvsEditor(this)

    override suspend operator fun contains(key: String): Boolean = synchronized(lock) {
        _map.containsKey(key)
    }

    internal fun commitEditor(editorData: Map<String, Any?>) = synchronized(lock) {
        val currentMap = _map.toMutableMap()
        editorData.forEach { (key, value) ->
            if (value == null) {
                currentMap.remove(key)
            } else {
                currentMap[key] = value
            }
        }
        data.store(currentMap.toMap())
    }

    internal class InMemoryKvsEditor(private val kvs: InMemoryKvs) : Kvs.KvsEditor {

        private val editorData: MutableMap<String, Any?> = mutableMapOf()

        override fun putString(key: String, value: String): Kvs.KvsEditor = apply {
            editorData[key] = value
        }

        override fun putInt(key: String, value: Int): Kvs.KvsEditor = apply {
            editorData[key] = value
        }

        override fun putLong(key: String, value: Long): Kvs.KvsEditor = apply {
            editorData[key] = value
        }

        override fun putFloat(key: String, value: Float): Kvs.KvsEditor = apply {
            editorData[key] = value
        }

        override fun putBoolean(key: String, value: Boolean): Kvs.KvsEditor = apply {
            editorData[key] = value
        }

        override fun remove(key: String): Kvs.KvsEditor = apply {
            editorData[key] = null // Mark for removal
        }

        override fun clear(): Kvs.KvsEditor = apply {
            editorData.clear()
            //To reflect clearing the original map, we need to mark all existing keys for removal
            kvs._map.keys.forEach { editorData[it] = null }
        }

        override suspend fun commit() {
            kvs.commitEditor(editorData)
        }
    }
}