package com.santimattius.kvs

import com.santimattius.kvs.internal.datastore.KvsStandard
import com.santimattius.kvs.internal.datastore.KvsStream
import com.santimattius.kvs.internal.exception.WriteKvsException
import kotlinx.coroutines.CancellationException

/**
 * Represents a Key-Value Storage system.
 * This interface provides methods to store, retrieve, and manage key-value pairs.
 */
interface Kvs : KvsStandard, KvsStream {

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
        @Throws(IllegalStateException::class)
        fun putString(key: String, value: String): KvsEditor

        /**
         * Sets an Int value in the editor.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putInt(key: String, value: Int): KvsEditor

        /**
         * Sets a Long value in the editor.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putLong(key: String, value: Long): KvsEditor

        /**
         * Sets a Float value in the editor.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putFloat(key: String, value: Float): KvsEditor

        /**
         * Sets a Boolean value in the editor.
         *
         * @param key The name of the preference to modify.
         * @param value The new value for the preference.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun putBoolean(key: String, value: Boolean): KvsEditor

        /**
         * Removes a preference value from the editor.
         *
         * @param key The name of the preference to remove.
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun remove(key: String): KvsEditor

        /**
         * Removes all preference values from the editor.
         *
         * @return This editor instance, to chain calls.
         */
        @Throws(IllegalStateException::class)
        fun clear(): KvsEditor

        /**
         * Commits the preference changes to persistent storage.
         * This operation is performed asynchronously.
         */
        @Throws(
            IllegalStateException::class,
            WriteKvsException::class,
            CancellationException::class
        )
        suspend fun commit()

    }

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