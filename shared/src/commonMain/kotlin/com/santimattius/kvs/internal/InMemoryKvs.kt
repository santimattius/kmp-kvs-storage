package com.santimattius.kvs.internal

import com.santimattius.kvs.Kvs
import com.santimattius.kvs.internal.datastore.KvsStandard
import com.santimattius.kvs.internal.datastore.KvsStream
import com.santimattius.kvs.internal.memory.InMemoryKvsStandard
import com.santimattius.kvs.internal.memory.InMemoryKvsStream
import com.santimattius.kvs.internal.memory.InMemoryPreferences
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * An in-memory implementation of the [Kvs] interface.
 * This implementation is intended for testing purposes and does not persist data.
 * @param preferences The in-memory preferences store.
 * @param lock A lock object for synchronization.
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal class InMemoryKvs(
    private val preferences: InMemoryPreferences,
    private val lock: SynchronizedObject
) : Kvs, KvsStandard by InMemoryKvsStandard(preferences, lock),
    KvsStream by InMemoryKvsStream(preferences) {

    override fun edit(): Kvs.KvsEditor = InMemoryKvsEditor(this)

    override suspend operator fun contains(key: String): Boolean = synchronized(lock) {
        preferences.contains(key)
    }

    internal fun commitEditor(editorData: Map<String, Any?>) = synchronized(lock) {
        preferences.commitEditor(editorData)
    }

    /**
     * An in-memory implementation of the [Kvs.KvsEditor] interface.
     * @param kvs The [InMemoryKvs] instance.
     */
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
            kvs.preferences.values.keys.forEach { editorData[it] = null }
        }

        override suspend fun commit() {
            kvs.commitEditor(editorData)
        }
    }
}