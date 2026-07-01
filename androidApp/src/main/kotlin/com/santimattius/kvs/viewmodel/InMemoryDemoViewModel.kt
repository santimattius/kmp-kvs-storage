package com.santimattius.kvs.viewmodel

import com.santimattius.kvs.Kvs

private const val DEMO_KEY = "demo_value"

/** Demo ViewModel for the [com.santimattius.kvs.Storage.inMemoryKvs] backend. */
class InMemoryDemoViewModel(private val kvs: Kvs) : StorageDemoViewModel() {

    override suspend fun performPut(value: String) {
        kvs.edit().putString(DEMO_KEY, value).commit()
    }

    override suspend fun performGet(): String = kvs.getString(DEMO_KEY, "")
}
