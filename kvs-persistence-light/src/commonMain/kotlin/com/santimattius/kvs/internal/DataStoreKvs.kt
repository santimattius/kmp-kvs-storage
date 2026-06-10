package com.santimattius.kvs.internal

import com.santimattius.kvs.Kvs
import com.santimattius.kvs.internal.KvsStandard
import com.santimattius.kvs.internal.KvsStream
import com.santimattius.kvs.internal.datastore.DataStoreKvsEditor
import com.santimattius.kvs.internal.datastore.DataStoreKvsStandard
import com.santimattius.kvs.internal.datastore.DataStoreKvsStream
import com.santimattius.kvs.internal.datastore.storage.Storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DataStoreKvs(
    private val dataStore: Storage<String>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Kvs,
    KvsStandard by DataStoreKvsStandard(dataStore),
    KvsStream by DataStoreKvsStream(dataStore) {

    override fun edit(): Kvs.KvsEditor = DataStoreKvsEditor(dataStore, dispatcher)

    override suspend fun contains(key: String): Boolean = withContext(dispatcher) {
        dataStore.contains(key)
    }
}
