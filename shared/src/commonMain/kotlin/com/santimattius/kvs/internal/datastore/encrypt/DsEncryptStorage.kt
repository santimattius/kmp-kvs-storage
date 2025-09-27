package com.santimattius.kvs.internal.datastore.encrypt

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.santimattius.kvs.internal.datastore.readPreference
import com.santimattius.kvs.internal.datastore.storage.Storage
import com.santimattius.kvs.internal.datastore.storage.StorageOperation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal class DsEncryptStorage(
    private val dataStore: DataStore<Preferences>,
    private val encryptor: Encryptor,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Storage<String> {

    override suspend fun getAll(): Map<String, Any> {
        val currentPreferences = dataStore.data.first()
        return currentPreferences.asMap().mapKeys { entry ->
            entry.key.name
        }.mapValues { entry ->
            encryptor.decrypt(entry.value.toString())
        }
    }

    override fun getAllAsStream(): Flow<Map<String, Any>> {
        return dataStore.data.map { preferences ->
            preferences.asMap().mapKeys { entry -> entry.key.name }
                .mapValues { entry -> encryptor.decrypt(entry.value.toString()) }
        }.flowOn(dispatcher)
    }

    override suspend fun edit(block: StorageOperation<String>.() -> Unit) {
        dataStore.edit { preferences ->
            val operation = DsEncryptStorageOperation(preferences, encryptor)
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
            converter = {
                converter(encryptor.decrypt(it))
            }
        )
    }

    override fun <T> readPreferenceAsStream(
        key: String,
        defaultValue: T,
        converter: (String) -> T?
    ): Flow<T> = dataStore.data.map { preferences ->
        preferences.readPreference(key, defaultValue, converter = {
            converter(encryptor.decrypt(it))
        })
    }.flowOn(dispatcher)

}

internal class DsEncryptStorageOperation(
    private val preferences: MutablePreferences,
    private val encryptor: Encryptor
) : StorageOperation<String> {

    override fun clear() {
        preferences.clear()
    }

    override fun remove(key: String) {
        preferences.remove(key(key))
    }

    override fun put(key: String, value: String) {
        preferences[key(key)] = encryptor.encrypt(value)
    }

    override fun key(name: String): Preferences.Key<String> {
        return stringPreferencesKey(name)
    }

}