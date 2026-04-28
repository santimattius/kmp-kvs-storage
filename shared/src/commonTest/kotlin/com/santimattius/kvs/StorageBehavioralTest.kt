package com.santimattius.kvs

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Behavioural tests for [Storage] factory methods.
 * Pattern: **Given** → **When** → **Then**. Validates that factories return working instances
 * and that isolation by name behaves as expected. Uses only [Storage.inMemoryKvs] to stay
 * deterministic and platform-agnostic in commonTest.
 */
class StorageBehavioralTest {

    @Test
    fun givenUniqueName_whenInMemoryKvs_thenReturnsWorkingKvs() = runTest {
        val kvs = Storage.inMemoryKvs("storage_factory_working")

        kvs.edit().putString("key", "value").commit()
        val result = kvs.getString("key", "def")

        assertEquals("value", result)
    }

    @Test
    fun givenSameName_whenInMemoryKvsTwice_thenSameInstanceReturned() = runTest {
        val name = "storage_same_instance"
        val kvs1 = Storage.inMemoryKvs(name)
        val kvs2 = Storage.inMemoryKvs(name)

        assertTrue(kvs1 === kvs2)
    }

    @Test
    fun givenDifferentNames_whenInMemoryKvs_thenIsolatedStores() = runTest {
        val kvsA = Storage.inMemoryKvs("storage_isolated_a")
        val kvsB = Storage.inMemoryKvs("storage_isolated_b")
        kvsA.edit().putString("only_in_a", "a").commit()
        kvsB.edit().putString("only_in_b", "b").commit()

        assertEquals("a", kvsA.getString("only_in_a", "def"))
        assertEquals("def", kvsA.getString("only_in_b", "def"))
        assertEquals("b", kvsB.getString("only_in_b", "def"))
        assertEquals("def", kvsB.getString("only_in_a", "def"))
    }

    @Test
    fun givenDebugDisabledByDefault_whenInMemoryKvs_thenOperationsSucceed() = runTest {
        val kvs = Storage.inMemoryKvs("storage_no_debug")
        kvs.edit().putInt("n", 1).commit()
        assertEquals(1, kvs.getInt("n", 0))
    }

    @Test
    fun givenDebugEnabled_whenInMemoryKvs_thenOperationsStillSucceed() = runTest {
        Storage.debug(true)
        val kvs = Storage.inMemoryKvs("storage_with_debug")
        kvs.edit().putBoolean("flag", true).commit()
        assertTrue(kvs.getBoolean("flag", false))
        Storage.debug(false)
    }
}
