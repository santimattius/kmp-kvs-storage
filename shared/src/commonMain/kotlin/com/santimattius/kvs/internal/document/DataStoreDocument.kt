package com.santimattius.kvs.internal.document

import androidx.datastore.core.DataStore
import com.santimattius.kvs.Document
import kotlinx.coroutines.flow.first

internal class DataStoreDocument(
    private val dataStore: DataStore<String>
) : Document {

    override suspend fun read(): String {
        return dataStore.data.first()
    }

    override suspend fun write(value: String) {
        dataStore.updateData { value }
    }
}