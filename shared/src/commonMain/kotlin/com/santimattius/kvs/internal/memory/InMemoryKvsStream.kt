package com.santimattius.kvs.internal.memory

import com.santimattius.kvs.internal.ds.KvsStream
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * An in-memory implementation of the [KvsStream] interface.
 *
 * This class provides Flow-based access to key-value pairs stored in an [InMemoryPreferences]
 * instance. It allows observing changes to the preferences data reactively.
 *
 * @property preferences The [InMemoryPreferences] instance holding the data and providing the base stream.
 */
@OptIn(InternalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal class InMemoryKvsStream(
    private val preferences: InMemoryPreferences,
) : KvsStream {

    /**
     * Returns a [Flow] that emits a map of all key-value pairs upon any change in the underlying [InMemoryPreferences].
     *
     * @return A [Flow] emitting a map of all preferences. Keys are preference names,
     *         and values are of type [Any], reflecting their original stored type.
     */
    override fun getAllAsStream(): Flow<Map<String, Any>> {
        return preferences.stream
    }

    /**
     * Returns a [Flow] that emits updates to a String preference value.
     *
     * The flow transforms the map emitted by [InMemoryPreferences.stream]
     * to extract the specific String value associated with the [key].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is not a String.
     * @return A [Flow] emitting the String preference value, or [defValue].
     */
    override fun getStringAsStream(
        key: String,
        defValue: String
    ): Flow<String> {
        return preferences.stream.map {
            it[key] as? String ?: defValue
        }
    }

    /**
     * Returns a [Flow] that emits updates to an Int preference value.
     *
     * The flow transforms the map emitted by [InMemoryPreferences.stream]
     * to extract the specific Int value associated with the [key].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is not an Int.
     * @return A [Flow] emitting the Int preference value, or [defValue].
     */
    override fun getIntAsStream(
        key: String,
        defValue: Int
    ): Flow<Int> {
        return preferences.stream.map {
            it[key] as? Int ?: defValue
        }
    }

    /**
     * Returns a [Flow] that emits updates to a Long preference value.
     *
     * The flow transforms the map emitted by [InMemoryPreferences.stream]
     * to extract the specific Long value associated with the [key].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is not a Long.
     * @return A [Flow] emitting the Long preference value, or [defValue].
     */
    override fun getLongAsStream(
        key: String,
        defValue: Long
    ): Flow<Long> {
        return preferences.stream.map {
            it[key] as? Long ?: defValue
        }
    }

    /**
     * Returns a [Flow] that emits updates to a Float preference value.
     *
     * The flow transforms the map emitted by [InMemoryPreferences.stream]
     * to extract the specific Float value associated with the [key].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is not a Float.
     * @return A [Flow] emitting the Float preference value, or [defValue].
     */
    override fun getFloatAsStream(
        key: String,
        defValue: Float
    ): Flow<Float> {
        return preferences.stream.map {
            it[key] as? Float ?: defValue
        }
    }

    /**
     * Returns a [Flow] that emits updates to a Boolean preference value.
     *
     * The flow transforms the map emitted by [InMemoryPreferences.stream]
     * to extract the specific Boolean value associated with the [key].
     *
     * @param key The name of the preference to observe.
     * @param defValue The default value to emit if the preference does not exist or is not a Boolean.
     * @return A [Flow] emitting the Boolean preference value, or [defValue].
     */
    override fun getBooleanAsStream(
        key: String,
        defValue: Boolean
    ): Flow<Boolean> {
        return preferences.stream.map {
            it[key] as? Boolean ?: defValue
        }
    }


}