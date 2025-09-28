package com.santimattius.kvs.internal.datastore.encrypt

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.santimattius.kvs.internal.datastore.readPreference
import com.santimattius.kvs.internal.datastore.storage.Storage
import com.santimattius.kvs.internal.datastore.storage.StorageOperation
import com.santimattius.kvs.internal.exception.ReadKvsException
import com.santimattius.kvs.internal.logger.KvsLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * A [Storage] implementation that uses [DataStore] to store preferences with encryption.
 * All values are stored as encrypted strings and decrypted when read.
 *
 * @param dataStore The [DataStore] instance to use for storing preferences.
 * @param encryptor The [Encryptor] instance to use for encrypting and decrypting values.
 * @param logger The [KvsLogger] instance to use for logging errors.
 * @param dispatcher The [CoroutineDispatcher] to use for I/O operations, defaults to [Dispatchers.IO].
 */
internal class DsEncryptStorage(
    private val dataStore: DataStore<Preferences>,
    private val encryptor: Encryptor,
    private val logger: KvsLogger,
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
        }.catch { cause ->
            logger.error("Error getting all preferences", cause)
            emit(emptyMap())
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
        return try {
            dataStore.readPreference(
                dispatcher = dispatcher,
                key = key,
                defaultValue = defaultValue,
                converter = {
                    converter(encryptor.decrypt(it))
                }
            )
        } catch (e: Throwable) {
            logger.error("Error reading preference", e)
            throw ReadKvsException("Error reading preference", e)
        }
    }

    override fun <T> readPreferenceAsStream(
        key: String,
        defaultValue: T,
        converter: (String) -> T?
    ): Flow<T> = dataStore.data.map { preferences ->
        preferences.readPreference(key, defaultValue, converter = {
            converter(encryptor.decrypt(it))
        })
    }.catch { cause ->
        logger.error("Error reading preference", cause)
        emit(defaultValue)
    }.flowOn(dispatcher)

}

/**
 * Internal class that provides encrypted storage operations for DataStore.
 *
 * This class implements the [StorageOperation] interface and handles
 * the encryption and decryption of data before storing or retrieving it
 * from the underlying [MutablePreferences]. It is used within the context
 * of the `edit` block in [DsEncryptStorage].
 *
 * @param preferences The [MutablePreferences] instance to store data in.
 * @param encryptor The [Encryptor] instance used for data encryption and decryption.
 */
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
        try {
            preferences[key(key)] = encryptor.encrypt(value)
        } catch (ex: Throwable) {
            throw EncryptException("Error encrypting value", ex)
        }
    }

    override fun key(name: String): Preferences.Key<String> {
        return stringPreferencesKey(name)
    }

}