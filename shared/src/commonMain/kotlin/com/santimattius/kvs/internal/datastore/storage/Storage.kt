package com.santimattius.kvs.internal.datastore.storage

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

internal interface Storage<T> {

    suspend fun getAll(): Map<String, Any>

    fun getAllAsStream(): Flow<Map<String, Any>>

    suspend fun edit(block: StorageOperation<T>.() -> Unit)

    suspend fun contains(key: String): Boolean

    suspend fun <V> readPreference(
        key: String,
        defaultValue: V,
        converter: (String) -> V?
    ): V

    fun <V> readPreferenceAsStream(
        key: String,
        defaultValue: V,
        converter: (String) -> V?
    ) : Flow<V>

}

internal interface StorageOperation<T> {

    fun clear()

    fun remove(key: String)

    fun put(key: String, value: T)

    fun key(name: String): Preferences.Key<T>
}
