package com.santimattius.kvs.internal.datastore

import com.santimattius.kvs.Kvs
import com.santimattius.kvs.internal.datastore.storage.Storage
import com.santimattius.kvs.internal.exception.WriteKvsException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile

internal class DataStoreKvsEditor(
    private val storage: Storage<String>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Kvs.KvsEditor {

    private val commitMutex = Mutex()
    private var clearOperation = false
    private val addValues = mutableMapOf<String, String>()
    private val removeValues = mutableListOf<String>()

    @Volatile private var committed = false
    @Volatile private var commitInProgress = false

    private fun checkModifyState() {
        if (committed) throw IllegalStateException("Editor has already been committed and cannot be modified.")
        if (commitInProgress) throw IllegalStateException("Editor cannot be modified while a commit is in progress.")
    }

    override fun putString(key: String, value: String): Kvs.KvsEditor {
        checkModifyState(); return putToAdd(key, value)
    }

    override fun putInt(key: String, value: Int): Kvs.KvsEditor {
        checkModifyState(); return putToAdd(key, value.toString())
    }

    override fun putLong(key: String, value: Long): Kvs.KvsEditor {
        checkModifyState(); return putToAdd(key, value.toString())
    }

    override fun putFloat(key: String, value: Float): Kvs.KvsEditor {
        checkModifyState(); return putToAdd(key, value.toString())
    }

    override fun putBoolean(key: String, value: Boolean): Kvs.KvsEditor {
        checkModifyState(); return putToAdd(key, value.toString())
    }

    override fun remove(key: String): Kvs.KvsEditor {
        checkModifyState(); return putToRemove(key)
    }

    override fun clear(): Kvs.KvsEditor {
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
                    storage.edit {
                        if (currentClearOperation) {
                            clear()
                        } else {
                            currentRemoveValues.forEach { key -> remove(key) }
                        }
                        currentAddValues.forEach { (key, value) -> put(key, value) }
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

    private fun putToAdd(key: String, value: String): Kvs.KvsEditor {
        addValues[key] = value
        removeValues.remove(key)
        return this
    }

    private fun putToRemove(key: String): Kvs.KvsEditor {
        removeValues.add(key)
        addValues.remove(key)
        return this
    }
}
