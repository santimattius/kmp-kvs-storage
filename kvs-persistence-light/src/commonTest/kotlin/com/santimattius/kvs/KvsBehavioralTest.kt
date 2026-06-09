package com.santimattius.kvs

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Behavioural tests for the [Kvs] public API.
 * Pattern: **Given** (preconditions) → **When** (action) → **Then** (expected outcome).
 * Uses [Storage.inMemoryKvs] for deterministic, in-memory behaviour without mocking.
 */
class KvsBehavioralTest {

    private fun kvs(name: String) = Storage.inMemoryKvs(name)

    // --- edit / commit / get ---

    @Test
    fun givenEmptyKvs_whenPutStringAndCommit_thenGetReturnsStoredValue() = runTest {
        val kvs = kvs("kvs_put_get_string")
        kvs.edit().putString("key", "value").commit()

        val result = kvs.getString("key", "default")

        assertEquals("value", result)
    }

    @Test
    fun givenEmptyKvs_whenGetMissingKey_thenReturnsDefValue() = runTest {
        val kvs = kvs("kvs_get_missing")
        val result = kvs.getString("missing", "def")
        assertEquals("def", result)
    }

    @Test
    fun givenKvsWithAllValueTypes_whenReadBack_thenAllTypesMatch() = runTest {
        val kvs = kvs("kvs_all_types")
        kvs.edit()
            .putString("s", "hello")
            .putInt("i", 42)
            .putLong("l", 123L)
            .putFloat("f", 3.14f)
            .putBoolean("b", true)
            .commit()

        assertEquals("hello", kvs.getString("s", ""))
        assertEquals(42, kvs.getInt("i", 0))
        assertEquals(123L, kvs.getLong("l", 0L))
        assertEquals(3.14f, kvs.getFloat("f", 0f))
        assertTrue(kvs.getBoolean("b", false))
    }

    // --- contains ---

    @Test
    fun givenKeyStored_whenContains_thenReturnsTrue() = runTest {
        val kvs = kvs("kvs_contains_true")
        kvs.edit().putString("k", "v").commit()

        val result = kvs.contains("k")

        assertTrue(result)
    }

    @Test
    fun givenKeyNotStored_whenContains_thenReturnsFalse() = runTest {
        val kvs = kvs("kvs_contains_false")

        val result = kvs.contains("missing")

        assertFalse(result)
    }

    // --- remove ---

    @Test
    fun givenKeyStored_whenRemoveAndCommit_thenGetReturnsDefValue() = runTest {
        val kvs = kvs("kvs_remove")
        kvs.edit().putString("k", "v").commit()
        kvs.edit().remove("k").commit()

        val result = kvs.getString("k", "def")

        assertEquals("def", result)
        assertFalse(kvs.contains("k"))
    }

    // --- clear ---

    @Test
    fun givenMultipleKeys_whenClearAndCommit_thenGetAllEmptyAndGetReturnsDef() = runTest {
        val kvs = kvs("kvs_clear")
        kvs.edit().putString("a", "1").putString("b", "2").commit()
        kvs.edit().clear().commit()

        val all = kvs.getAll()
        val aResult = kvs.getString("a", "def")

        assertTrue(all.isEmpty())
        assertEquals("def", aResult)
    }

    // --- getAll ---

    @Test
    fun givenEmptyKvs_whenGetAll_thenReturnsEmptyMap() = runTest {
        val kvs = kvs("kvs_get_all_empty")

        val result = kvs.getAll()

        assertTrue(result.isEmpty())
    }

    @Test
    fun givenKeysStored_whenGetAll_thenReturnsAllEntries() = runTest {
        val kvs = kvs("kvs_get_all")
        kvs.edit().putString("a", "1").putInt("b", 2).putBoolean("c", true).commit()

        val result = kvs.getAll()

        assertEquals(3, result.size)
        assertEquals("1", result["a"])
        assertEquals(2, result["b"])
        assertEquals(true, result["c"])
    }

    // --- Stream APIs ---

    @Test
    fun givenKeyStored_whenGetStringAsStreamFirst_thenEmitsStoredValue() = runTest {
        val kvs = kvs("kvs_stream_string")
        kvs.edit().putString("key", "streamValue").commit()

        val result = kvs.getStringAsStream("key", "def").first()

        assertEquals("streamValue", result)
    }

    @Test
    fun givenKeyMissing_whenGetStringAsStreamFirst_thenEmitsDefValue() = runTest {
        val kvs = kvs("kvs_stream_missing")

        val result = kvs.getStringAsStream("missing", "def").first()

        assertEquals("def", result)
    }

    @Test
    fun givenKeysStored_whenGetAllAsStreamFirst_thenEmitsCurrentMap() = runTest {
        val kvs = kvs("kvs_stream_all")
        kvs.edit().putString("x", "y").commit()

        val map = kvs.getAllAsStream().first()

        assertEquals(1, map.size)
        assertEquals("y", map["x"])
    }

    @Test
    fun givenMultipleTypesStored_whenGetTypedAsStream_thenEachReturnsCorrectValue() = runTest {
        val kvs = kvs("kvs_stream_types")
        kvs.edit()
            .putInt("i", 10)
            .putLong("l", 20L)
            .putFloat("f", 1.5f)
            .putBoolean("b", false)
            .commit()

        assertEquals(10, kvs.getIntAsStream("i", 0).first())
        assertEquals(20L, kvs.getLongAsStream("l", 0L).first())
        assertEquals(1.5f, kvs.getFloatAsStream("f", 0f).first())
        assertFalse(kvs.getBooleanAsStream("b", true).first())
    }

    // --- Editor chaining ---

    @Test
    fun givenChainedEditorCalls_whenCommit_thenAllChangesApplied() = runTest {
        val kvs = kvs("kvs_chain")
        kvs.edit()
            .putString("a", "1")
            .putString("b", "2")
            .remove("a")
            .commit()

        assertEquals("def", kvs.getString("a", "def"))
        assertEquals("2", kvs.getString("b", "def"))
    }
}
