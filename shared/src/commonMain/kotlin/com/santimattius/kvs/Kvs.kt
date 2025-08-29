package com.santimattius.kvs

import com.santimattius.kvs.internal.DataStoreKvs
import com.santimattius.kvs.internal.provideDataStoreInstance

/**
 * Represents a Key-Value Storage system.
 * This interface provides methods to store, retrieve, and manage key-value pairs.
 */
interface Kvs {

    /**
     * An editor for modifying values in the Key-Value Storage.
     * Changes made through the editor are applied atomically when [commit] is called.
     */
    interface KvsEditor {

        /**
         * Sets a String value in the editor.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        fun putString(key: String, value: String): KvsEditor

        /**
         * Sets an Int value in the editor.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        fun putInt(key: String, value: Int): KvsEditor

        /**
         * Sets a Long value in the editor.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        fun putLong(key: String, value: Long): KvsEditor

        /**
         * Sets a Float value in the editor.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        fun putFloat(key: String, value: Float): KvsEditor

        /**
         * Sets a Boolean value in the editor.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        fun putBoolean(key: String, value: Boolean): KvsEditor

        /**
         * Removes a preference value from the editor.
         *
         * @param key The name of the preference to remove.
         * @return This editor instance, to chain calls.
         */
        fun remove(key: String): KvsEditor

        /**
         * Removes all preference values from the editor.
         *
         * @return This editor instance, to chain calls.
         */
        fun clear(): KvsEditor

        /**
         * Commits the preference changes to persistent storage.
         * This operation is performed asynchronously.
         */
        suspend fun commit()

    }

    /**
     * Retrieves all key-value pairs stored.
     *
     * @return A map containing all stored preferences. The values are of type [Any],
     * reflecting the type they were stored with (e.g., String, Int, Boolean).
     */
    suspend fun getAll(): Map<String, Any>

    /**
     * Retrieves a String value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getString(key: String, defValue: String): String

    /**
     * Retrieves an Int value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getInt(key: String, defValue: Int): Int

    /**
     * Retrieves a Long value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getLong(key: String, defValue: Long): Long

    /**
     * Retrieves a Float value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getFloat(key: String, defValue: Float): Float

    /**
     * Retrieves a Boolean value from the storage.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue The default value to return if the preference does not exist.
     * @return The preference value if it exists, or [defValue].
     */
    suspend fun getBoolean(key: String, defValue: Boolean): Boolean

    /**
     * Creates a new [KvsEditor] for modifying preferences.
     *
     * @return A new [KvsEditor] instance.
     */
    fun edit(): KvsEditor

    /**
     * Checks if the storage contains a preference with the given key.
     *
     * @param key The key to check.
     * @return `true` if the key exists, `false` otherwise.
     */
    suspend operator fun contains(key: String): Boolean

}