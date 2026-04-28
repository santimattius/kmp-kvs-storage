@file:OptIn(com.santimattius.kvs.ExperimentalKvsTtl::class)
package com.santimattius.kvs.internal.ttl.extended

import androidx.datastore.core.DataStore
import com.santimattius.kvs.KvsExtended
import com.santimattius.kvs.internal.KvsStandard
import com.santimattius.kvs.internal.KvsStream
import com.santimattius.kvs.internal.ttl.CleanupJob
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.internal.ttl.cleanup.TtlCleanupJob
import kotlinx.coroutines.flow.first
import kotlin.time.Duration

internal class TtlKvsExtended(
    private val dataStore: DataStore<Map<String, TTLEntity>>,
    private val ttlManager: TtlManager
) : KvsExtended,
    KvsStandard by TtlKvsExtendedStandard(dataStore, ttlManager),
    KvsStream by TtlKvsExtendedStream(dataStore, ttlManager) {

    override fun edit(): KvsExtended.KvsExtendedEditor = TtlKvsExtendedEditor(dataStore, ttlManager)

    override suspend fun contains(key: String): Boolean {
        val entity = dataStore.data.first()[key] ?: return false
        val expiresAt = entity.expiresAt ?: return true
        return !ttlManager.isExpired(expiresAt)
    }

    override fun cleanupJob(interval: Duration): CleanupJob =
        TtlCleanupJob(dataStore, ttlManager, interval)
}
