package com.santimattius.kvs.internal.memory

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
class InMemoryPreferences(private val lock: SynchronizedObject) {

    private val _data: AtomicReference<Map<String, Any>> = AtomicReference(emptyMap())
    private val _stream: MutableStateFlow<Map<String, Any>> = MutableStateFlow(emptyMap())

    val values: Map<String, Any> get() = _data.load()

    val stream: Flow<Map<String, Any>> get() = _stream

    fun put(key: String, value: Any) {
        while (true) {
            val current = _data.load()
            val updated = current + (key to value)
            if (_data.compareAndSet(current, updated)) {
                _stream.value = updated
                return
            }
        }
    }

    fun get(key: String, defValue: Any): Any = values[key] ?: defValue

    fun contains(key: String): Boolean = values.containsKey(key)

    internal fun commitEditor(editorData: Map<String, Any?>) = synchronized(lock) {
        val updated = values.toMutableMap().also { map ->
            editorData.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
        }.toMap()
        _data.store(updated)
        _stream.value = updated
    }
}
