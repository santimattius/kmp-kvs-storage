package com.santimattius.kvs.internal.datastore

import com.santimattius.kvs.Kvs
import com.santimattius.kvs.internal.datastore.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile

internal class DataStoreKvsEditor(
    private val storage: Storage<String>
) : Kvs.KvsEditor {

    // Mutex to ensure atomic commit operation
    private val commitMutex = Mutex()

    private var clearOperation = false

    // Note: These collections are not inherently thread-safe if modification methods (putString, etc.)
    // are called concurrently on the same instance before commit() is invoked.
    private val addValues = mutableMapOf<String, String>()
    private val removeValues = mutableListOf<String>()

    @Volatile
    private var committed = false

    @Volatile
    private var commitInProgress = false

    private fun checkModifyState() {
        if (committed) throw IllegalStateException("Editor has already been committed and cannot be modified.")
        if (commitInProgress) throw IllegalStateException("Editor cannot be modified while a commit is in progress.")
    }

    override fun putString(
        key: String,
        value: String
    ): Kvs.KvsEditor {
        checkModifyState()
        return putToAdd(key, value)
    }

    override fun putInt(key: String, value: Int): Kvs.KvsEditor {
        checkModifyState()
        return putToAdd(key, value.toString())
    }

    override fun putLong(
        key: String,
        value: Long
    ): Kvs.KvsEditor {
        checkModifyState()
        return putToAdd(key, value.toString())
    }

    override fun putFloat(
        key: String,
        value: Float
    ): Kvs.KvsEditor {
        checkModifyState()
        return putToAdd(key, value.toString())
    }

    override fun putBoolean(
        key: String,
        value: Boolean
    ): Kvs.KvsEditor {
        checkModifyState()
        return putToAdd(key, value.toString())
    }

    override fun remove(key: String): Kvs.KvsEditor {
        checkModifyState()
        return putToRemove(key)
    }

    override fun clear(): Kvs.KvsEditor {
        checkModifyState()
        clearOperation = true
        return this
    }

    override suspend fun commit() {
        commitMutex.withLock {
            if (committed) {
                throw IllegalStateException("Editor has already been committed.")
            }
            commitInProgress = true // Signal that commit is in progress

            // Capture the current state for the commit operation to ensure consistency
            val currentAddValues = HashMap(this.addValues)
            val currentRemoveValues = ArrayList(this.removeValues)
            val currentClearOperation = this.clearOperation

            try {
                //TODO: review this context change
                withContext(Dispatchers.IO) {
                    storage.edit {
                        if (currentClearOperation) {
                            clear()
                            // After clearing, we still want to apply any pending additions from this session.
                            // Removals for this session are implicitly handled by the clear.
                        } else {
                            currentRemoveValues.forEach { key ->
                                remove(key)
                            }
                        }
                        currentAddValues.forEach { (key, value) ->
                            put(key, value)
                        }
                    }
                }
                // If commit is successful, mark as committed and clear internal state for this instance
                committed = true
                this.addValues.clear()
                this.removeValues.clear()
                this.clearOperation = false
            } finally {
                commitInProgress = false // Reset commit in progress flag
            }
        }
    }

    private fun putToAdd(key: String, value: String): Kvs.KvsEditor {
        // This method is called by public methods that already performed checkModifyState()
        addValues[key] = value
        removeValues.remove(key)
        return this
    }

    private fun putToRemove(key: String): Kvs.KvsEditor {
        // This method is called by public methods that already performed checkModifyState()
        removeValues.add(key)
        addValues.remove(key)
        return this
    }
}