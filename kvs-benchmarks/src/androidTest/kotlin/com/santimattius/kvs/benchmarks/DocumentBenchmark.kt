@file:OptIn(androidx.benchmark.ExperimentalBenchmarkConfigApi::class)

package com.santimattius.kvs.benchmarks

import androidx.benchmark.MicrobenchmarkConfig
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.santimattius.kvs.Document
import com.santimattius.kvs.Storage
import com.santimattius.kvs.document
import com.santimattius.kvs.get
import com.santimattius.kvs.put
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmarks for the single-document JSON backend ([Storage.document]).
 *
 * `put`/`get` measure the typed [Document.put]/[Document.get] round trip (JSON
 * serialize-then-write, read-then-deserialize) for a small [BenchmarkProfile] payload.
 */
@RunWith(AndroidJUnit4::class)
class DocumentBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule(MicrobenchmarkConfig(measurementCount = MEASUREMENT_COUNT))

    private lateinit var document: Document

    @Before
    fun setup() {
        document = Storage.document("bench-document-${System.nanoTime()}")
    }

    @Test
    fun put() = benchmarkRule.measureRepeated {
        runBlocking {
            document.put(SAMPLE_PROFILE)
        }
    }

    @Test
    fun get() {
        runBlocking { document.put(SAMPLE_PROFILE) }

        benchmarkRule.measureRepeated {
            val result = runBlocking { document.get<BenchmarkProfile>() }
            check(result == SAMPLE_PROFILE) { "Expected $SAMPLE_PROFILE, got $result" }
        }
    }

    private companion object {
        const val MEASUREMENT_COUNT = 10
        val SAMPLE_PROFILE = BenchmarkProfile(name = "Santiago", age = 30)
    }
}

@Serializable
internal data class BenchmarkProfile(val name: String, val age: Int)
