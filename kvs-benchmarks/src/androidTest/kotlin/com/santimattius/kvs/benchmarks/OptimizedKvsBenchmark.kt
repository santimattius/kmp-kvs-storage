@file:OptIn(androidx.benchmark.ExperimentalBenchmarkConfigApi::class)

package com.santimattius.kvs.benchmarks

import androidx.benchmark.MicrobenchmarkConfig
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.santimattius.kvs.InternalKvsApi
import com.santimattius.kvs.Kvs
import com.santimattius.kvs.Storage
import com.santimattius.kvs.kvsOptimized
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmarks for the **optimized** (SQLDelight-backed) persistence backend
 * ([Storage.kvsOptimized]).
 *
 * The Android context required by the underlying `AndroidSqliteDriver` is supplied
 * automatically by `kvs-persistence-optimized`'s App Startup [androidx.startup.Initializer]
 * (`OptimizedKvsInitializer`), merged into this module's instrumented test APK manifest —
 * no manual `AndroidSqliteDriver`/context wiring is required here.
 */
@OptIn(InternalKvsApi::class)
@RunWith(AndroidJUnit4::class)
class OptimizedKvsBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule(MicrobenchmarkConfig(measurementCount = MEASUREMENT_COUNT))

    private lateinit var kvs: Kvs

    @Before
    fun setup() {
        kvs = Storage.kvsOptimized("bench-optimized-${System.nanoTime()}")
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
