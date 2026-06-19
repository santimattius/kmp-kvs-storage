package com.santimattius.kvs

import androidx.datastore.core.DataStore
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.internal.ttl.cleanup.TtlCleanupJob
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.concurrent.Volatile
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Cancellation behavioural tests for [TtlCleanupJob].
 *
 * STRICT TDD: Tests in the "AC-A1 (RED)" section are expected to FAIL before the
 * fix because `catch (_: Exception)` swallows `CancellationException`, allowing the
 * cleanup body to continue executing after the scope was cancelled.
 *
 * The core violation: when `dataStore.updateData { ... }` throws `CancellationException`
 * (e.g. mid-write), the broad catch swallows it. The loop body then runs AGAIN on the
 * next iteration even though the parent scope is cancelled, producing writes that should
 * not happen post-cancellation.
 *
 * Fix required: `catch (e: CancellationException) { throw e }` BEFORE the broad catch.
 *
 * AC-A1: When the parent coroutine scope is cancelled while a cleanup write is
 * in-flight, `CancellationException` MUST propagate — the cleanup body MUST NOT
 * continue executing after cancellation.
 */
class TtlCleanupJobCancellationTest {

    /**
     * A DataStore that:
     * - Returns one expired entry via [data] (so cleanup always reaches [updateData]).
     * - Suspends in [updateData] until cancelled — forcing `CancellationException` to
     *   be thrown inside the try/catch block.
     * - Records how many times [updateData] was called via [updateCallCount].
     *
     * After a scope cancel, if [updateData] is called MORE THAN ONCE, the exception was
     * swallowed and the loop re-entered — the bug is confirmed.
     */
    private class ObservableBlockingDataStore : DataStore<Map<String, TTLEntity>> {

        @Volatile var updateCallCount = 0
        @Volatile var cancellationExceptionReceivedInUpdate = false

        private val expired = mapOf(
            "key" to TTLEntity("key", "value", expiresAt = 0L)
        )

        override val data: StateFlow<Map<String, TTLEntity>> =
            MutableStateFlow(expired).asStateFlow()

        override suspend fun updateData(
            transform: suspend (Map<String, TTLEntity>) -> Map<String, TTLEntity>
        ): Map<String, TTLEntity> {
            updateCallCount++
            try {
                // Suspend indefinitely — CancellationException thrown here when cancelled.
                suspendCancellableCoroutine<Nothing> { }
            } catch (e: CancellationException) {
                cancellationExceptionReceivedInUpdate = true
                throw e // always re-throw in the fake; we test whether the caller re-throws
            }
        }
    }

    // AC-A1 ─────────────────────────────────────────────────────────────────

    /**
     * RED before fix:
     * - `CancellationException` thrown by [ObservableBlockingDataStore.updateData] is caught
     *   by `catch (_: Exception)` in [TtlCleanupJob].
     * - The loop re-enters; [updateData] is called a second time.
     * - `updateCallCount > 1` → test fails with "Cleanup body executed after cancellation".
     *
     * GREEN after fix:
     * - `CancellationException` re-thrown → loop exits immediately.
     * - `updateCallCount == 1` → test passes.
     */
    @Test
    fun givenCleanupJobBlockedInUpdateData_whenScopeCancelled_thenCleanupBodyDoesNotReenter() =
        runBlocking {
            // Given: a cleanup job whose updateData suspends indefinitely and records calls
            val store = ObservableBlockingDataStore()
            val ttlManager = TtlManager(defaultTtl = 10.seconds)
            val cleanupJob = TtlCleanupJob(
                dataStore = store,
                ttlManager = ttlManager,
                interval = 1.milliseconds  // minimal delay so re-entry is observable fast
            )

            val jobScope = CoroutineScope(Job())
            val job = cleanupJob.start(jobScope)

            // Allow the coroutine to enter updateData (first call).
            delay(100)

            // When: the parent scope is cancelled while updateData is suspended.
            jobScope.cancel()

            // Wait for the job to terminate.
            withTimeout(2.seconds) { job.join() }

            // Then: updateData must NOT have been called more than once.
            // A second call means the loop re-entered after the CancellationException
            // was swallowed — a structured concurrency violation.
            assertTrue(
                store.cancellationExceptionReceivedInUpdate,
                "updateData must have received a CancellationException (pre-condition)"
            )
            assertFalse(
                store.updateCallCount > 1,
                "Cleanup body must NOT re-enter after cancellation " +
                        "(updateData called ${store.updateCallCount} times, expected 1)"
            )
        }

    // AC-A1 (always GREEN) ───────────────────────────────────────────────────

    @Test
    fun givenCleanupJobRunning_whenScopeCancelled_thenJobTransitionsToCancelled() =
        runBlocking {
            // Given: a cleanup job with a very short interval (non-blocking writes)
            val store = com.santimattius.kvs.internal.ttl.InMemoryTtlDataStore()
            val ttlManager = TtlManager(defaultTtl = 10.seconds)
            val cleanupJob = TtlCleanupJob(store, ttlManager, interval = 1.milliseconds)

            val jobScope = CoroutineScope(Job())
            val job = cleanupJob.start(jobScope)

            delay(100)

            // When: cancel the scope
            jobScope.cancel()

            // Then: job reaches terminal cancelled state
            withTimeout(2.seconds) { job.join() }
            assertTrue(job.isCancelled, "Job must transition to Cancelled state")
        }

    @Test
    fun givenCleanupJobRunning_whenJobCancelledDirectly_thenJobIsCancelled() =
        runBlocking {
            // Given: a cleanup job running
            val store = com.santimattius.kvs.internal.ttl.InMemoryTtlDataStore()
            val ttlManager = TtlManager(defaultTtl = 10.seconds)
            val cleanupJob = TtlCleanupJob(store, ttlManager, interval = 1.milliseconds)

            val jobScope = CoroutineScope(Job())
            val job = cleanupJob.start(jobScope)

            delay(100)

            // When: the job is cancelled directly
            job.cancel()

            // Then: job is in cancelled state
            withTimeout(2.seconds) { job.join() }
            assertTrue(job.isCancelled, "Job must be Cancelled after direct cancel()")
        }
}
