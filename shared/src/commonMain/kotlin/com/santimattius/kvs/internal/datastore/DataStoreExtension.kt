package com.santimattius.kvs.internal.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Reads a preference value associated with the given [key].
 *
 * This is a generic private function used by the public getter methods.
 * It retrieves a string preference and then uses the provided [converter]
 * to transform it into the desired type [T].
 *
 * @param T The target data type.
 * @param key The preference key.
 * @param defaultValue The value to return if the key is not found or conversion fails.
 * @param converter A function to convert the String value to type [T].
 * @return The converted preference value, or [defaultValue] if not found or conversion fails.
 */
internal suspend fun <T> DataStore<Preferences>.readPreference(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    key: String,
    defaultValue: T,
    converter: (String) -> T?
): T = withContext(dispatcher) {
    // Use data.first() to get the current snapshot of preferences.
    // Using .last() on DataStore's Flow would suspend indefinitely.
    val currentPreferences = data.first()
    currentPreferences.readPreference(key, defaultValue, converter)
}

/**
 * Reads a preference value from the current [Preferences] snapshot.
 *
 * This is a private extension function for [Preferences] used by [readPreferenceFlow].
 * It retrieves a string preference and then uses the provided [converter]
 * to transform it into the desired type [T].
 *
 * @param T The target data type.
 * @param key The preference key.
 * @param defaultValue The value to return if the key is not found or conversion fails.
 * @param converter A function to convert the String value to type [T].
 * @return The converted preference value, or [defaultValue] if not found or conversion fails.
 */
internal fun <T> Preferences.readPreference(
    key: String,
    defaultValue: T,
    converter: (String) -> T?
): T {
    val preferencesKey = stringPreferencesKey(key)
    val stringValue = get(preferencesKey)
    return if (stringValue != null) {
        converter(stringValue) ?: defaultValue
    } else {
        defaultValue
    }
}