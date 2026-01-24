package com.santimattius.kvs.internal.ttl.extended

import androidx.datastore.core.DataStore
import com.santimattius.kvs.KvsExtended
import com.santimattius.kvs.internal.datastore.storage.Storage
import com.santimattius.kvs.internal.exception.WriteKvsException
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile
import kotlin.time.Duration

/**
 * Implementation of [KvsExtended.KvsExtendedEditor] for DataStore with TTL support.
 *
 * This class provides methods to modify key-value pairs in a batch with optional TTL configuration.
 * Changes are not applied until [commit] is called. The editor supports both per-key TTL
 * specification and automatic application of default TTL when no duration is provided.
 *
 * **TTL Behavior:**
 * - If a [Duration] is provided, that specific TTL is used for the key
 * - If no [Duration] is provided, the [TtlManager.defaultTtl] is applied (if configured)
 * - If both are `null`, the key will not expire
 *
 * **Thread Safety:**
 * This editor is not thread-safe for concurrent modifications before [commit].
 * It uses a [Mutex] to ensure that the [commit] operation itself is atomic.
 *
 * Once [commit] has been called, the editor instance can no longer be used for further modifications.
 *
 * @property storage The underlying [DataStore] implementation where data will be persisted.
 * @property ttlManager The [TtlManager] instance used for TTL calculations.
 * @property dispatcher The [CoroutineDispatcher] used for I/O operations during commit. Defaults to [Dispatchers.IO].
 */
internal class TtlKvsExtendedEditor(
    private val storage: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : KvsExtended.KvsExtendedEditor {

    // Mutex to ensure atomic commit operation
    private val commitMutex = Mutex()

    private var clearOperation = false

    // Note: These collections are not inherently thread-safe if modification methods (putString, etc.)
    // are called concurrently on the same instance before commit() is invoked.
    private val addValues = mutableMapOf<String, TTLEntity>()
    private val removeValues = mutableListOf<String>()

    @Volatile
    private var committed = false

    @Volatile
    private var commitInProgress = false

    private fun checkModifyState() {
        if (committed) throw IllegalStateException("Editor has already been committed and cannot be modified.")
        if (commitInProgress) throw IllegalStateException("Editor cannot be modified while a commit is in progress.")
    }

    /**
     * Sets a String value in the editor.
     *
     * If no [duration] is provided, the [TtlManager.defaultTtl] will be applied (if configured).
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @return This editor instance, to chain calls.
     */
    override fun putString(
        key: String,
        value: String
    ): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, value)
    }

    /**
     * Sets a String value in the editor with a specific TTL duration.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @param duration The TTL duration for this specific key.
     * @return This editor instance, to chain calls.
     */
    override fun putString(
        key: String,
        value: String,
        duration: Duration
    ): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, TTLEntity(key, value, duration))
    }

    /**
     * Sets an Int value in the editor.
     *
     * If no [duration] is provided, the [TtlManager.defaultTtl] will be applied (if configured).
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @return This editor instance, to chain calls.
     */
    override fun putInt(key: String, value: Int): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, value.toString())
    }

    /**
     * Sets an Int value in the editor with a specific TTL duration.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @param duration The TTL duration for this specific key.
     * @return This editor instance, to chain calls.
     */
    override fun putInt(
        key: String,
        value: Int,
        duration: Duration
    ): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, TTLEntity(key, value.toString(), duration))
    }

    /**
     * Sets a Long value in the editor.
     *
     * If no [duration] is provided, the [TtlManager.defaultTtl] will be applied (if configured).
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @return This editor instance, to chain calls.
     */
    override fun putLong(
        key: String,
        value: Long
    ): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, value.toString())
    }

    /**
     * Sets a Long value in the editor with a specific TTL duration.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @param duration The TTL duration for this specific key.
     * @return This editor instance, to chain calls.
     */
    override fun putLong(
        key: String,
        value: Long,
        duration: Duration
    ): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, TTLEntity(key, value.toString(), duration))
    }

    /**
     * Sets a Float value in the editor.
     *
     * If no [duration] is provided, the [TtlManager.defaultTtl] will be applied (if configured).
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @return This editor instance, to chain calls.
     */
    override fun putFloat(
        key: String,
        value: Float
    ): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, value.toString())
    }

    /**
     * Sets a Float value in the editor with a specific TTL duration.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @param duration The TTL duration for this specific key.
     * @return This editor instance, to chain calls.
     */
    override fun putFloat(
        key: String,
        value: Float,
        duration: Duration
    ): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, TTLEntity(key, value.toString(), duration))
    }

    /**
     * Sets a Boolean value in the editor.
     *
     * If no [duration] is provided, the [TtlManager.defaultTtl] will be applied (if configured).
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @return This editor instance, to chain calls.
     */
    override fun putBoolean(
        key: String,
        value: Boolean
    ): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, value.toString())
    }

    /**
     * Sets a Boolean value in the editor with a specific TTL duration.
     *
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     * @param duration The TTL duration for this specific key.
     * @return This editor instance, to chain calls.
     */
    override fun putBoolean(
        key: String,
        value: Boolean,
        duration: Duration
    ): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToAdd(key, TTLEntity(key, value.toString(), duration))
    }

    /**
     * Removes a preference value from the editor.
     *
     * @param key The name of the preference to remove.
     * @return This editor instance, to chain calls.
     */
    override fun remove(key: String): KvsExtended.KvsExtendedEditor {
        checkModifyState()
        return putToRemove(key)
    }

    /**
     * Removes all preference values from the editor.
     *
     * @return This editor instance, to chain calls.
     */
    override fun clear(): KvsExtended.KvsExtendedEditor {
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
                withContext(dispatcher) {
                    storage.updateData { current ->
                        current.toMutableMap().apply {
                            if (currentClearOperation) {
                                clear()
                                // After clearing, we still want to apply any pending additions from this session.
                                // Removals for this session are implicitly handled by the clear.
                            } else {
                                currentRemoveValues.forEach { key ->
                                    remove(key)
                                }
                            }
                            putAll(currentAddValues)
                        }
                    }
                }
                // If commit is successful, mark as committed and clear internal state for this instance
                committed = true
                this.addValues.clear()
                this.removeValues.clear()
                this.clearOperation = false
            } catch (e: Throwable) {
                throw WriteKvsException(message = "Error writing to storage", e)
            } finally {
                commitInProgress = false // Reset commit in progress flag
            }
        }
    }

    /**
     * Adds a value to the pending changes without explicit TTL.
     *
     * The [TtlManager.defaultTtl] will be applied if configured.
     *
     * @param key The key to add.
     * @param value The value as a String.
     * @return This editor instance.
     */
    private fun putToAdd(key: String, value: String): KvsExtended.KvsExtendedEditor {
        // Create entity without duration - calculateExpiration will use defaultTtl if available
        return putToAdd(key, TTLEntity(key, value, duration = null))
    }

    /**
     * Adds a [TTLEntity] to the pending changes and calculates its expiration timestamp.
     *
     * If [value.duration] is `null`, the [TtlManager.defaultTtl] will be used.
     *
     * @param key The key to add.
     * @param value The [TTLEntity] to add.
     * @return This editor instance.
     */
    private fun putToAdd(key: String, value: TTLEntity): KvsExtended.KvsExtendedEditor {
        // calculateExpiration will use defaultTtl when value.duration is null
        val expiresAt = ttlManager.calculateExpiration(value.duration)
        addValues[key] = value.copy(expiresAt = expiresAt)
        removeValues.remove(key)
        return this
    }

    private fun putToRemove(key: String): KvsExtended.KvsExtendedEditor {
        // This method is called by public methods that already performed checkModifyState()
        removeValues.add(key)
        addValues.remove(key)
        return this
    }
}