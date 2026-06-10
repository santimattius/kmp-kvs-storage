package com.santimattius.kvs.internal.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal suspend fun <T> DataStore<Preferences>.readPreference(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    key: String,
    defaultValue: T,
    converter: (String) -> T?
): T = withContext(dispatcher) {
    val currentPreferences = data.first()
    currentPreferences.readPreference(key, defaultValue, converter)
}

internal fun <T> Preferences.readPreference(
    key: String,
    defaultValue: T,
    converter: (String) -> T?
): T {
    val preferencesKey = stringPreferencesKey(key)
    val stringValue = get(preferencesKey)
    return if (stringValue != null) converter(stringValue) ?: defaultValue else defaultValue
}
