package com.santimattius.kvs.internal.ttl.cleanup

import androidx.datastore.core.DataStore
import com.santimattius.kvs.internal.ttl.CleanupJob
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal class TtlCleanupJob(
    private val dataStore: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager,
    private val interval: Duration = 10.minutes
) : CleanupJob {

    override fun start(scope: CoroutineScope): Job {
        return scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    cleanupExpiredKeys()
                } catch (_: Exception) {
                    // continue running on error
                }
                delay(interval)
            }
        }
    }

    private suspend fun cleanupExpiredKeys() {
        val all = dataStore.data.first()
        val expiredKeys = buildSet {
            for ((k, entity) in all) {
                if (entity.expiresAt != null && ttlManager.isExpired(entity.expiresAt)) add(k)
            }
        }

        if (expiredKeys.isNotEmpty()) {
            dataStore.updateData { data ->
                data.toMutableMap().apply { expiredKeys.forEach { remove(it) } }
            }
        }
    }
}
