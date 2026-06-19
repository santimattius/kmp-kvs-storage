package com.santimattius.kvs

import com.santimattius.kvs.internal.TtlBatchCleanupJob
import com.santimattius.kvs.persistence.optimized.db.KvsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Cancellation behavioural tests for [TtlBatchCleanupJob].
 *
 * STRICT TDD: These tests were written BEFORE the fix and document the
 * structured concurrency contract that MUST hold after applying the fix.
 *
 * AC-A2: When the parent coroutine scope is cancelled, `CancellationException`
 * MUST propagate so the job transitions to the `Cancelled` state within one
 * loop-iteration boundary.
 *
 * Note: [TtlBatchCleanupJob] calls `queries.deleteExpired(...)` which is a
 * synchronous SQLDelight operation. Cancellation therefore arrives at the
 * `delay(interval)` suspension point rather than inside the `try` block.
 * The broad `catch (_: Exception)` is still incorrect per structured-concurrency
 * guidelines — it would suppress `CancellationException` if the body ever becomes
 * asynchronous. The fix is applied preemptively to ensure long-term correctness.
 */
class TtlBatchCleanupJobCancellationTest {

    private lateinit var db: KvsDatabase

    @BeforeTest
    fun setUp() {
        db = KvsDatabase(createTestSqlDriver())
    }

    @AfterTest
    fun tearDown() {
        db.kvsEntryQueries.deleteAll()
    }

    // AC-A2 ─────────────────────────────────────────────────────────────────

    @Test
    fun givenCleanupJobRunning_whenScopeCancelled_thenJobTransitionsToCancelled() = runBlocking {
        // Given: a batch cleanup job running with a very short interval
        val cleanupJob = TtlBatchCleanupJob(db.kvsEntryQueries, interval = 1.milliseconds)
        val jobScope = CoroutineScope(Job())
        val job = cleanupJob.start(jobScope)

        // Allow the job to perform at least one cleanup cycle.
        delay(100)

        // When: the parent scope is cancelled
        jobScope.cancel()

        // Then: the job must reach a terminal (cancelled) state without hanging.
        withTimeout(2.seconds) {
            job.join()
        }
        assertTrue(job.isCancelled, "Job must transition to Cancelled state after scope cancel")
    }

    @Test
    fun givenCleanupJobRunning_whenJobCancelledDirectly_thenJobIsCancelled() = runBlocking {
        // Given: a batch cleanup job running
        val cleanupJob = TtlBatchCleanupJob(db.kvsEntryQueries, interval = 1.milliseconds)
        val jobScope = CoroutineScope(Job())
        val job = cleanupJob.start(jobScope)

        delay(100)

        // When: the specific coroutine job is cancelled directly
        job.cancel()

        // Then: job.join() returns and job is in cancelled state
        withTimeout(2.seconds) {
            job.join()
        }
        assertTrue(job.isCancelled, "Job must be in Cancelled state after direct cancel()")
    }

    @Test
    fun givenExpiredKeysPresent_whenJobRunsThenCancelled_thenExpiredKeysAreRemoved() = runBlocking {
        // Given: two expired keys in the store and the job running
        db.kvsEntryQueries.upsert("dead1", "v1", 0L)
        db.kvsEntryQueries.upsert("dead2", "v2", 0L)
        val cleanupJob = TtlBatchCleanupJob(db.kvsEntryQueries, interval = 1.milliseconds)
        val jobScope = CoroutineScope(Job())
        val job = cleanupJob.start(jobScope)

        // When: let the job run briefly, then cancel
        delay(100)
        jobScope.cancel()

        withTimeout(2.seconds) {
            job.join()
        }

        // Then: expired keys have been removed and job is in cancelled state
        val remaining = db.kvsEntryQueries.selectAll().executeAsList()
        assertTrue(remaining.isEmpty(), "Expired keys must have been removed before cancellation")
        assertTrue(job.isCancelled, "Job must be in Cancelled state")
    }
}
