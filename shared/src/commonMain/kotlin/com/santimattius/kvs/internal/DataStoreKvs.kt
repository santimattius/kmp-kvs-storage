package com.santimattius.kvs.internal

import androidx.datastore.core.DataStore
import com.santimattius.kvs.Kvs
import com.santimattius.kvs.internal.datastore.DataStoreKvsEditor
import com.santimattius.kvs.internal.datastore.DataStoreKvsStandard
import com.santimattius.kvs.internal.datastore.DataStoreKvsStream
import com.santimattius.kvs.internal.datastore.KvsStandard
import com.santimattius.kvs.internal.datastore.KvsStream
import com.santimattius.kvs.internal.datastore.storage.Storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
    private val dataStore: Storage<String>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Kvs, KvsStandard by DataStoreKvsStandard(dataStore),
    KvsStream by DataStoreKvsStream(dataStore) {

    /**
     * Returns a [Kvs.KvsEditor] for editing preferences.
     *
     * @return A [com.santimattius.kvs.internal.datastore.DataStoreKvsEditor] instance.
     */
    override fun edit(): Kvs.KvsEditor {
        return DataStoreKvsEditor(dataStore, dispatcher)
    }

    /**
     * Checks if the storage contains a value for the given key.
     *
     * @param key The key to check.
     * @return `true` if the key exists, `false` otherwise.
     */
    override suspend fun contains(key: String): Boolean = withContext(dispatcher) {
        dataStore.contains(key)
    }
}
