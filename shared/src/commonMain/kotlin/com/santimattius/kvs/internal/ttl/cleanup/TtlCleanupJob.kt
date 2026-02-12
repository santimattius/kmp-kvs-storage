package com.santimattius.kvs.internal.ttl.cleanup

import androidx.datastore.core.DataStore
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

/**
 * Interface for background cleanup jobs that remove expired keys from storage.
 *
 * Implementations should provide a way to start the cleanup process in a given [CoroutineScope].
 * The returned [Job] can be used to cancel or monitor the cleanup job.
 */
interface CleanupJob {
    /**
     * Starts the cleanup job in the provided [CoroutineScope].
     *
     * @param scope The [CoroutineScope] to run the cleanup job in.
     * @return A [Job] instance that can be used to cancel or monitor the cleanup job.
     *
     * @sample
     * ```
     * val cleanupJob = kvs.cleanupJob(Duration.ofMinutes(10))
     * val job = cleanupJob.start(coroutineScope)
     * // Later, to cancel:
     * job.cancel()
     * ```
     */
    fun start(scope: CoroutineScope): Job
}

/**
 * Background cleanup job that periodically removes expired keys from TTL-enabled storage.
 *
 * This job runs in the background on [Dispatchers.IO] and periodically scans the storage
 * for expired keys, removing them in a single batch. Running off the main thread avoids
 * UI jank and reduces GC pressure on the main thread.
 *
 * **Use Cases:**
 * - High-volume scenarios where keys may not be accessed frequently
 * - Long TTL durations where keys might accumulate before expiration
 * - Applications that need proactive cleanup without user interaction
 *
 * @property dataStore The [DataStore] instance to clean up.
 * @property ttlManager The [TtlManager] instance for expiration checks.
 * @property interval The interval between cleanup runs. Defaults to 10 minutes.
 *
 * @sample
 * ```
 * val cleanupJob = TtlCleanupJob(dataStore, ttlManager, Duration.ofMinutes(5))
 * cleanupJob.start(coroutineScope)
 * ```
 */
internal class TtlCleanupJob(
    private val dataStore: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager,
    private val interval: Duration = 10.minutes
) : CleanupJob {
    /**
     * Starts the cleanup job in the provided scope.
     *
     * The job will run indefinitely, performing cleanup at the configured [interval].
     * Errors during cleanup are caught and logged, but the job continues running.
     * The returned [Job] can be used to cancel the cleanup job.
     *
     * @param scope The [CoroutineScope] to run the cleanup job in.
     * @return A [Job] instance that can be used to cancel or monitor the cleanup job.
     */
    override fun start(scope: CoroutineScope): Job {
        return scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    cleanupExpiredKeys()
                } catch (_: Exception) {
                    // Log error but continue running
                }
                delay(interval)
            }
        }
    }

    /**
     * Performs cleanup of expired keys from the storage.
     *
     * Scans all keys, identifies expired ones, and removes them in a single batch.
     * Runs on the same dispatcher as the job ([Dispatchers.IO]) to keep I/O off the main thread.
     */
    private suspend fun cleanupExpiredKeys() {
        val all = dataStore.data.first()
        val expiredKeys = buildSet {
            for ((k, entity) in all) {
                if (entity.expiresAt != null && ttlManager.isExpired(entity.expiresAt)) add(k)
            }
        }

        if (expiredKeys.isNotEmpty()) {
            dataStore.updateData { data ->
                data.toMutableMap().apply {
                    expiredKeys.forEach { remove(it) }
                }
            }
        }
    }
}
