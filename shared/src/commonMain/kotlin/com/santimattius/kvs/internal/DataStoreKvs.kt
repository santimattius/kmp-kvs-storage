package com.santimattius.kvs.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.santimattius.kvs.Kvs
import kotlinx.coroutines.flow.first

internal class DataStoreKvs(
    private val dataStore: DataStore<Preferences>
) : Kvs {

    override suspend fun getAll(): Map<String, Any> {
        val currentPreferences = dataStore.data.first()
        return currentPreferences.asMap().mapKeys { entry ->
            entry.key.name
        }
    }

    private suspend fun <T> readPreference(
        key: String,
        defaultValue: T,
        converter: (String) -> T?
    ): T {
        val preferencesKey = stringPreferencesKey(key)
        // Use data.first() to get the current snapshot of preferences.
        // Using .last() on DataStore's Flow would suspend indefinitely.
        val currentPreferences = dataStore.data.first()
        val stringValue = currentPreferences[preferencesKey]
        return if (stringValue != null) {
            converter(stringValue) ?: defaultValue
        } else {
            defaultValue
        }
    }

    override suspend fun getString(key: String, defValue: String): String {
        return readPreference(key, defValue) { it }
    }

    override suspend fun getInt(key: String, defValue: Int): Int {
        return readPreference(key, defValue) { it.toIntOrNull() }
    }

    override suspend fun getLong(key: String, defValue: Long): Long {
        return readPreference(key, defValue) { it.toLongOrNull() }
    }

    override suspend fun getFloat(key: String, defValue: Float): Float {
        return readPreference(key, defValue) { it.toFloatOrNull() }
    }

    override suspend fun getBoolean(key: String, defValue: Boolean): Boolean {
        return readPreference(key, defValue) { it.toBooleanStrictOrNull() }
    }

    override fun edit(): Kvs.KvsEditor {
        return DataStoreKvsEditor(dataStore)
    }

    override suspend fun contains(key: String): Boolean {
        val preferencesKey = stringPreferencesKey(key)
        val currentPreferences = dataStore.data.first()
        return currentPreferences.contains(preferencesKey)
    }
}
