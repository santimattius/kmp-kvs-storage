package com.santimattius.kvs.internal.ttl

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory [DataStore] for [Map][String], [TTLEntity]] used in TTL behavioral tests.
 * Provides deterministic, fast storage without platform file system.
 */
internal class InMemoryTtlDataStore(
    initial: Map<String, TTLEntity> = emptyMap()
) : DataStore<Map<String, TTLEntity>> {

    private val state = MutableStateFlow(initial)
    private val mutex = Mutex()

    override val data: StateFlow<Map<String, TTLEntity>> = state.asStateFlow()

    override suspend fun updateData(transform: suspend (Map<String, TTLEntity>) -> Map<String, TTLEntity>): Map<String, TTLEntity> {
        mutex.withLock {
            val next = transform(state.value)
            state.value = next
            return next
        }
    }
}
