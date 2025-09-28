package com.santimattius.kvs.internal.memory

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * An in-memory implementation of a preferences store.
 *
 * This class manages a thread-safe map of key-value pairs and provides
 * a [Flow] to observe changes to the data.
 * It uses an [AtomicReference] for the underlying data storage and a [SynchronizedObject]
 * for commit operations to ensure thread safety.
 *
 * @property lock A [SynchronizedObject] used to protect critical sections when committing editor changes.
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
class InMemoryPreferences(
    private val lock: SynchronizedObject
) {
    private val _data: AtomicReference<Map<String, Any>> = AtomicReference(emptyMap())

    // It will only emit the initial 'values'. Consider using a StateFlow updated in 'put' and 'commitEditor'.
    private val _stream: MutableStateFlow<Map<String, Any>> = MutableStateFlow(values)

    /**
     * Gets the current snapshot of all stored preferences.
     *
     * @return An immutable [Map] of the current preferences.
     */
    val values: Map<String, Any>
        get() = _data.load()

    /**
     * A [Flow] that emits the current snapshot of all preferences whenever they change.
     * Note: Currently, this Flow might not update correctly on all data modifications.
     */
    val stream: Flow<Map<String, Any>>
        get() = _stream

    /**
     * Puts a key-value pair into the preferences.
     * This operation is atomic for the direct update to the backing AtomicReference,
     * but it does not update the [_stream] currently.
     *
     * @param key The key to store.
     * @param value The value to associate with the key.
     */
    fun put(key: String, value: Any) {
        // This loop ensures that the update is atomic using compareAndSet.
        // It reads the current map, creates a modified version, and then attempts to set it.
        // If another thread modified the map in the meantime, compareAndSet fails, and it retries.
        while (true) {
            val currentMap = _data.load()
            val newMap = currentMap.toMutableMap()
            newMap[key] = value
            if (_data.compareAndSet(currentMap, newMap.toMap())) {
                // Successfully updated, now also update the stream
                _stream.value = newMap.toMap() // Update the stream
                return
            }
        }
    }

    /**
     * Retrieves a value associated with the given key.
     *
     * @param key The key whose associated value is to be returned.
     * @param defValue The default value to return if the key is not found.
     * @return The value associated with the key, or [defValue] if the key is not present.
     */
    fun get(key: String, defValue: Any): Any {
        return values[key] ?: defValue
    }

    /**
     * Checks if the preferences contain a value for the given key.
     *
     * @param key The key to check.
     * @return `true` if the key exists, `false` otherwise.
     */
    fun contains(key: String): Boolean {
        return values.containsKey(key)
    }

    /**
     * Commits a batch of editor changes to the preferences.
     * This operation is synchronized using the provided [lock].
     * Null values in [editorData] indicate that the corresponding key should be removed.
     *
     * @param editorData A map of changes to apply. Keys are preference keys,
     *                   and values are the new preference values or null to remove the key.
     */
    internal fun commitEditor(editorData: Map<String, Any?>) = synchronized(lock) {
        val currentMap = values.toMutableMap()
        editorData.forEach { (key, value) ->
            if (value == null) {
                currentMap.remove(key)
            } else {
                currentMap[key] = value
            }
        }
        val newMapSnapshot = currentMap.toMap()
        _data.store(newMapSnapshot)
        _stream.value = newMapSnapshot // Update the stream
    }
}