package com.santimattius.kvs.internal.datastore.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.santimattius.kvs.internal.datastore.readPreference
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class DsStorage(
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Storage<String> {

    override suspend fun getAll(): Map<String, Any> {
        val currentPreferences = dataStore.data.first()
        return currentPreferences.asMap().mapKeys { entry ->
            entry.key.name
        }
    }

    override fun getAllAsStream(): Flow<Map<String, Any>> {
        return dataStore.data.map { preferences ->
            preferences.asMap().mapKeys { entry -> entry.key.name }
        }.flowOn(dispatcher)
    }

    override suspend fun edit(block: StorageOperation<String>.() -> Unit) {
        dataStore.edit { preferences ->
            val operation = DsStorageOperation(preferences)
            operation.block()
        }
    }

    override suspend fun contains(key: String): Boolean {
        val preferencesKey = stringPreferencesKey(key)
        val currentPreferences = dataStore.data.first()
        return currentPreferences.contains(preferencesKey)
    }

    override suspend fun <V> readPreference(
        key: String,
        defaultValue: V,
        converter: (String) -> V?
    ): V {
        return dataStore.readPreference(
            dispatcher = dispatcher,
            key = key,
            defaultValue = defaultValue,
            converter = converter
        )
    }

    override fun <T> readPreferenceAsStream(
        key: String,
        defaultValue: T,
        converter: (String) -> T?
    ): Flow<T> = dataStore.data.map { preferences ->
        preferences.readPreference(key, defaultValue, converter)
    }.flowOn(dispatcher)

}

internal class DsStorageOperation(
    private val preferences: MutablePreferences
) : StorageOperation<String> {

    override fun clear() {
        preferences.clear()
    }

    override fun remove(key: String) {
        preferences.remove(key(key))
    }

    override fun put(key: String, value: String) {
        preferences[key(key)] = value
    }

    override fun key(name: String): Preferences.Key<String> {
        return stringPreferencesKey(name)
    }

}