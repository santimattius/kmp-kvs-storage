@file:OptIn(androidx.benchmark.ExperimentalBenchmarkConfigApi::class)

package com.santimattius.kvs.benchmarks

import androidx.benchmark.MicrobenchmarkConfig
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.santimattius.kvs.ExperimentalKvsTtl
import com.santimattius.kvs.InternalKvsApi
import com.santimattius.kvs.KvsExtended
import com.santimattius.kvs.Storage
import com.santimattius.kvs.kvsLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * Benchmarks the [KvsExtended.getAll] hot path on a TTL-enabled store holding 500 entries,
 * 250 of which are expired (AC-E4).
 *
 * **Why expired entries are re-seeded on every iteration, not once in [setup]:**
 * `TtlKvsExtendedStandard.getAll()` (the `kvs-persistence-light` TTL implementation) both
 * scans AND evicts expired entries as a side effect of a single call — see
 * `com.santimattius.kvs.internal.ttl.extended.TtlKvsExtendedStandard.getAll`. If the 250
 * expired entries were seeded only once before the benchmark loop, the FIRST
 * `measureRepeated` iteration would pay the real scan+delete cost, but every iteration
 * after that would scan an already-clean, 250-entry store — silently measuring a cheaper
 * best case instead of the real hot path. To keep every iteration representative, the 250
 * expired entries are re-written immediately before each timed call, inside
 * [androidx.benchmark.MicrobenchmarkScope.runWithMeasurementDisabled] so that reseed cost is
 * excluded from the measurement itself.
 */
@OptIn(ExperimentalKvsTtl::class, InternalKvsApi::class)
@RunWith(AndroidJUnit4::class)
class TtlHotPathBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule(MicrobenchmarkConfig(measurementCount = MEASUREMENT_COUNT))

    private lateinit var kvs: KvsExtended

    @Before
    fun setup() {
        val defaultTtl: Duration? = null
        kvs = Storage.kvsLight(
            name = "bench-ttl-${System.nanoTime()}",
            ttl = defaultTtl,
            encrypted = false
        )
        runBlocking { seedLiveEntries() }
    }

    @Test
    fun getAllEvictsExpiredEntries() = benchmarkRule.measureRepeated {
        runWithMeasurementDisabled {
            runBlocking { seedExpiredEntries() }
        }

        val result = runBlocking { kvs.getAll() }
        check(result.size == LIVE_ENTRY_COUNT) {
            "Expected $LIVE_ENTRY_COUNT live entries after eviction, got ${result.size}"
        }
    }

    /** 250 entries that never expire during the benchmark run (long TTL). */
    private suspend fun seedLiveEntries() {
        var editor = kvs.edit()
        repeat(LIVE_ENTRY_COUNT) { index ->
            editor = editor.putString("live-$index", "value-$index", LONG_TTL)
        }
        editor.commit()
    }

    /**
     * 250 entries with a very short TTL, followed by a grace-period delay so they are
     * guaranteed to be wall-clock expired by the time the timed `getAll()` call runs.
     */
    private suspend fun seedExpiredEntries() {
        var editor = kvs.edit()
        repeat(EXPIRED_ENTRY_COUNT) { index ->
            editor = editor.putString("expired-$index", "value-$index", SHORT_TTL)
        }
        editor.commit()
        delay(EXPIRY_GRACE_PERIOD)
    }

    private companion object {
        const val LIVE_ENTRY_COUNT = 250
        const val EXPIRED_ENTRY_COUNT = 250
        const val MEASUREMENT_COUNT = 5
        val LONG_TTL = 10.minutes
        val SHORT_TTL = 5.milliseconds
        val EXPIRY_GRACE_PERIOD = 20.milliseconds
    }
}
