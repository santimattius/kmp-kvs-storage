@file:OptIn(androidx.benchmark.ExperimentalBenchmarkConfigApi::class)

package com.santimattius.kvs.benchmarks

import androidx.benchmark.MicrobenchmarkConfig
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.santimattius.kvs.InternalKvsApi
import com.santimattius.kvs.Kvs
import com.santimattius.kvs.Storage
import com.santimattius.kvs.kvsLight
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmarks for the **light** (DataStore-backed) persistence backend ([Storage.kvsLight]).
 *
 * The Android context required by DataStore is supplied automatically by
 * `kvs-persistence-light`'s App Startup [androidx.startup.Initializer]
 * (`AppContextInitializer`), whose manifest entry is merged into this module's
 * instrumented test APK because `kvs-benchmarks` depends on `kvs-persistence-light` via
 * `androidTestImplementation`. No manual context injection is required here — this
 * mirrors exactly how `androidApp` consumes the same factory.
 */
@OptIn(InternalKvsApi::class)
@RunWith(AndroidJUnit4::class)
class LightKvsBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule(MicrobenchmarkConfig(measurementCount = MEASUREMENT_COUNT))

    private lateinit var kvs: Kvs

    @Before
    fun setup() {
        kvs = Storage.kvsLight("bench-light-${System.nanoTime()}")
    }

    @Test
    fun put() = benchmarkRule.measureRepeated {
        runBlocking {
            kvs.edit().putString(SINGLE_KEY, SINGLE_VALUE).commit()
        }
    }

    @Test
    fun get() {
        runBlocking { kvs.edit().putString(SINGLE_KEY, SINGLE_VALUE).commit() }

        benchmarkRule.measureRepeated {
            runBlocking { kvs.getString(SINGLE_KEY, "") }
        }
    }

    @Test
    fun getAll() {
        runBlocking { seed(kvs, SEED_SIZE) }

        benchmarkRule.measureRepeated {
            val result = runBlocking { kvs.getAll() }
            check(result.size == SEED_SIZE) { "Expected $SEED_SIZE entries, got ${result.size}" }
        }
    }

    private companion object {
        const val SEED_SIZE = 100
        const val MEASUREMENT_COUNT = 10
        const val SINGLE_KEY = "single-key"
        const val SINGLE_VALUE = "single-value"
    }
}
