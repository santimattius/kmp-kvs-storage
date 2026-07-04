@file:OptIn(androidx.benchmark.ExperimentalBenchmarkConfigApi::class)

package com.santimattius.kvs.benchmarks

import androidx.benchmark.MicrobenchmarkConfig
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.santimattius.kvs.Kvs
import com.santimattius.kvs.Storage
import com.santimattius.kvs.kvsLightEncrypt
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmarks for the **encrypted light** persistence backend ([Storage.kvsLightEncrypt]).
 *
 * Only `put`/`get` are benchmarked (per AC-E2) — the encryption/decryption cost is what
 * differentiates this backend from [LightKvsBenchmark], so `getAll` (a bulk scan, not
 * representative of the per-entry crypto overhead) is intentionally out of scope here.
 *
 * The secret key is a plain benchmark fixture string, matching how [Storage.kvsLightEncrypt]
 * is actually invoked elsewhere in this codebase (see `KvsLightEncryptDemoViewModel`) — the
 * factory does not require Android Keystore access, only a caller-supplied key string.
 */
@RunWith(AndroidJUnit4::class)
class EncryptedKvsBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule(MicrobenchmarkConfig(measurementCount = MEASUREMENT_COUNT))

    private lateinit var kvs: Kvs

    @Before
    fun setup() {
        kvs = Storage.kvsLightEncrypt(
            name = "bench-encrypted-${System.nanoTime()}",
            secretKey = SECRET_KEY
        )
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

    private companion object {
        const val MEASUREMENT_COUNT = 10
        const val SINGLE_KEY = "single-key"
        const val SINGLE_VALUE = "single-value"
        const val SECRET_KEY = "kvs-benchmarks-secret-key"
    }
}
