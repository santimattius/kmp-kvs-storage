package com.santimattius.kvs.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.santimattius.kvs.Kvs
import com.santimattius.kvs.internal.ds.DataStoreKvsStandard
import com.santimattius.kvs.internal.ds.DataStoreKvsStream
import com.santimattius.kvs.internal.ds.KvsStandard
import com.santimattius.kvs.internal.ds.KvsStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * An implementation of [Kvs] that uses [DataStore] to store data.
 *
 * This class provides methods to store and retrieve data using Jetpack DataStore.
 * It delegates standard and stream operations to [DataStoreKvsStandard] and [DataStoreKvsStream] respectively.
 *
 * @property dataStore The [DataStore] instance used for data persistence.
 * @property dispatcher The [CoroutineDispatcher] used for background operations, defaults to [Dispatchers.IO].
 */
internal class DataStoreKvs(
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Kvs, KvsStandard by DataStoreKvsStandard(dataStore, dispatcher),
    KvsStream by DataStoreKvsStream(dataStore, dispatcher) {

    /**
     * Returns a [Kvs.KvsEditor] for editing preferences.
     *
     * @return A [DataStoreKvsEditor] instance.
     */
    override fun edit(): Kvs.KvsEditor {
        return DataStoreKvsEditor(dataStore)
    }

    /**
     * Checks if the storage contains a value for the given key.
     *
     * @param key The key to check.
     * @return `true` if the key exists, `false` otherwise.
     */
    override suspend fun contains(key: String): Boolean = withContext(dispatcher) {
        val preferencesKey = stringPreferencesKey(key)
        val currentPreferences = dataStore.data.first()
        currentPreferences.contains(preferencesKey)
    }
}
