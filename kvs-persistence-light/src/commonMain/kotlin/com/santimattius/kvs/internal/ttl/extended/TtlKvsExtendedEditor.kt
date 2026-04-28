@file:OptIn(com.santimattius.kvs.ExperimentalKvsTtl::class)
package com.santimattius.kvs.internal.ttl.extended

import androidx.datastore.core.DataStore
import com.santimattius.kvs.KvsExtended
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.internal.exception.WriteKvsException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile
import kotlin.time.Duration

internal class TtlKvsExtendedEditor(
    private val storage: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : KvsExtended.KvsExtendedEditor {

    private val commitMutex = Mutex()
    private var clearOperation = false
    private val addValues = mutableMapOf<String, TTLEntity>()
    private val removeValues = mutableListOf<String>()

    @Volatile private var committed = false
    @Volatile private var commitInProgress = false

    private fun checkModifyState() {
        if (committed) throw IllegalStateException("Editor has already been committed and cannot be modified.")
        if (commitInProgress) throw IllegalStateException("Editor cannot be modified while a commit is in progress.")
    }

    override fun putString(key: String, value: String): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, value)
    }

    override fun putString(key: String, value: String, duration: Duration): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, TTLEntity(key, value, duration))
    }

    override fun putInt(key: String, value: Int): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, value.toString())
    }

    override fun putInt(key: String, value: Int, duration: Duration): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, TTLEntity(key, value.toString(), duration))
    }

    override fun putLong(key: String, value: Long): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, value.toString())
    }

    override fun putLong(key: String, value: Long, duration: Duration): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, TTLEntity(key, value.toString(), duration))
    }

    override fun putFloat(key: String, value: Float): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, value.toString())
    }

    override fun putFloat(key: String, value: Float, duration: Duration): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, TTLEntity(key, value.toString(), duration))
    }

    override fun putBoolean(key: String, value: Boolean): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, value.toString())
    }

    override fun putBoolean(key: String, value: Boolean, duration: Duration): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToAdd(key, TTLEntity(key, value.toString(), duration))
    }

    override fun remove(key: String): KvsExtended.KvsExtendedEditor {
        checkModifyState(); return putToRemove(key)
    }

    override fun clear(): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        clearOperation = true
        return this
    }

    override suspend fun commit() {
        commitMutex.withLock {
            if (committed) throw IllegalStateException("Editor has already been committed.")
            commitInProgress = true

            val currentAddValues = HashMap(addValues)
            val currentRemoveValues = ArrayList(removeValues)
            val currentClearOperation = clearOperation

            try {
                withContext(dispatcher) {
                    storage.updateData { current ->
                        current.toMutableMap().apply {
                            if (currentClearOperation) {
                                clear()
                            } else {
                                currentRemoveValues.forEach { key -> remove(key) }
                            }
                            putAll(currentAddValues)
                        }
                    }
                }
                committed = true
                addValues.clear()
                removeValues.clear()
                clearOperation = false
            } catch (e: Throwable) {
                throw WriteKvsException(message = "Error writing to storage", e)
            } finally {
                commitInProgress = false
            }
        }
    }

    private fun putToAdd(key: String, value: String): KvsExtended.KvsExtendedEditor =
        putToAdd(key, TTLEntity(key, value, duration = null))

    private fun putToAdd(key: String, value: TTLEntity): KvsExtended.KvsExtendedEditor {
        val expiresAt = ttlManager.calculateExpiration(value.duration)
        addValues[key] = value.copy(expiresAt = expiresAt)
        removeValues.remove(key)
        return this
    }

    private fun putToRemove(key: String): KvsExtended.KvsExtendedEditor {
        removeValues.add(key)
        addValues.remove(key)
        return this
    }
}
