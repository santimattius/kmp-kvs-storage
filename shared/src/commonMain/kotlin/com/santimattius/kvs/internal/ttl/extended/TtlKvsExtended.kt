package com.santimattius.kvs.internal.ttl.extended

import androidx.datastore.core.DataStore
import com.santimattius.kvs.KvsExtended
import com.santimattius.kvs.internal.datastore.KvsStandard
import com.santimattius.kvs.internal.datastore.KvsStream
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.internal.ttl.cleanup.CleanupJob
import com.santimattius.kvs.internal.ttl.cleanup.TtlCleanupJob
import kotlinx.coroutines.flow.first
import kotlin.time.Duration

/**
 * Implementation of [KvsExtended] with Time-To-Live (TTL) support.
 *
 * This class provides a key-value storage system that supports automatic expiration
 * of stored values based on TTL configurations. It delegates standard and stream
 * operations to [TtlKvsExtendedStandard] and [TtlKvsExtendedStream] respectively.
 *
 * Features:
 * - Automatic expiration of keys based on TTL
 * - Support for default TTL at instance level
 * - Per-key TTL override capability
 * - Lazy cleanup of expired keys on access
 * - Background cleanup job support
 *
 * @property dataStore The [DataStore] instance used for persistence, storing [TTLEntity] objects.
 * @property ttlManager The [TtlManager] instance used for TTL calculations and expiration checks.
 *
 * @sample
 * ```
 * val kvs = TtlKvsExtended(
 *     dataStore = provideTtlDataStoreInstance("cache", Encryptor.None),
 *     ttlManager = TtlManager(defaultTtl = Duration.ofHours(1))
 * )
 * ```
 */
internal class TtlKvsExtended(
    private val dataStore: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager
) : KvsExtended, KvsStandard by TtlKvsExtendedStandard(dataStore, ttlManager),
    KvsStream by TtlKvsExtendedStream(dataStore, ttlManager) {

    /**
     * Creates a new [KvsExtended.KvsExtendedEditor] for batch modifications.
     *
     * @return A new [TtlKvsExtendedEditor] instance.
     */
    override fun edit(): KvsExtended.KvsExtendedEditor {
        return TtlKvsExtendedEditor(dataStore, ttlManager)
    }

    /**
     * Checks if the storage contains a non-expired value for the given key.
     *
     * This method verifies both the existence of the key and whether it has expired.
     * Expired keys are considered as not present.
     *
     * @param key The key to check.
     * @return `true` if the key exists and has not expired, `false` otherwise.
     */
    override suspend fun contains(key: String): Boolean {
        val entity = dataStore.data.first()[key] ?: return false
        val expiresAt = entity.expiresAt ?: return true // If no expiresAt, key is valid
        return !ttlManager.isExpired(expiresAt)
    }

    /**
     * Creates a background cleanup job for expired keys.
     *
     * The cleanup job will periodically scan and remove expired keys from storage.
     * This is useful for high-volume scenarios where keys may not be accessed frequently.
     *
     * @param interval The interval between cleanup runs.
     * @return A [CleanupJob] instance that can be started with a [kotlinx.coroutines.CoroutineScope].
     *         The [CleanupJob.start] method returns a [kotlinx.coroutines.Job] that can be used
     *         to cancel or monitor the cleanup process.
     *
     * @sample
     * ```
     * val cleanupJob = kvs.cleanupJob(Duration.ofMinutes(10))
     * val job = cleanupJob.start(coroutineScope)
     * // Later, to cancel:
     * job.cancel()
     * ```
     */
    override fun cleanupJob(interval: Duration): CleanupJob {
        return TtlCleanupJob(dataStore, ttlManager, interval)
    }
}