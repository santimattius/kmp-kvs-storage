package com.santimattius.kvs.internal

import com.santimattius.kvs.internal.ttl.CleanupJob
import com.santimattius.kvs.persistence.optimized.db.KvsEntryQueries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal class TtlBatchCleanupJob(
    private val queries: KvsEntryQueries,
    private val interval: Duration = 10.minutes
) : CleanupJob {

    override fun start(scope: CoroutineScope): Job = scope.launch(Dispatchers.Default) {
        while (isActive) {
            try {
                queries.deleteExpired(Clock.System.now().toEpochMilliseconds())
            } catch (_: Exception) {
                // continue running on error
            }
            delay(interval)
        }
    }
}
