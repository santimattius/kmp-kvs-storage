package com.santimattius.kvs

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Behavioural tests for [Kvs] extension APIs (Result-based getters and [Kvs.KvsEditor.apply]).
 * Pattern: **Given** → **When** → **Then**. Uses [Storage.inMemoryKvs] for deterministic behaviour.
 */
class KvsExtensionsBehavioralTest {

    private fun kvs(name: String) = Storage.inMemoryKvs(name)

    @Test
    fun givenKeyStored_whenGetStringAsResult_thenSuccessWithValue() = runTest {
        val kvs = kvs("ext_get_string_result")
        kvs.edit().putString("key", "value").commit()

        val result = kvs.getStringAsResult("key", "def")

        assertTrue(result.isSuccess)
        assertEquals("value", result.getOrThrow())
    }

    @Test
    fun givenKeyMissing_whenGetStringAsResult_thenSuccessWithDefValue() = runTest {
        val kvs = kvs("ext_get_string_missing")

        val result = kvs.getStringAsResult("missing", "default")

        assertTrue(result.isSuccess)
        assertEquals("default", result.getOrThrow())
    }

    @Test
    fun givenIntStored_whenGetIntAsResult_thenSuccessWithValue() = runTest {
        val kvs = kvs("ext_get_int_result")
        kvs.edit().putInt("i", 99).commit()

        val result = kvs.getIntAsResult("i", 0)

        assertTrue(result.isSuccess)
        assertEquals(99, result.getOrThrow())
    }

    @Test
    fun givenLongStored_whenGetLongAsResult_thenSuccessWithValue() = runTest {
        val kvs = kvs("ext_get_long_result")
        kvs.edit().putLong("l", 12345L).commit()

        val result = kvs.getLongAsResult("l", 0L)

        assertTrue(result.isSuccess)
        assertEquals(12345L, result.getOrThrow())
    }

    @Test
    fun givenFloatStored_whenGetFloatAsResult_thenSuccessWithValue() = runTest {
        val kvs = kvs("ext_get_float_result")
        kvs.edit().putFloat("f", 2.5f).commit()

        val result = kvs.getFloatAsResult("f", 0f)

        assertTrue(result.isSuccess)
        assertEquals(2.5f, result.getOrThrow())
    }

    @Test
    fun givenBooleanStored_whenGetBooleanAsResult_thenSuccessWithValue() = runTest {
        val kvs = kvs("ext_get_bool_result")
        kvs.edit().putBoolean("b", true).commit()

        val result = kvs.getBooleanAsResult("b", false)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun givenKeysStored_whenGetAllAsResult_thenSuccessWithMap() = runTest {
        val kvs = kvs("ext_get_all_result")
        kvs.edit().putString("a", "1").putInt("b", 2).commit()

        val result = kvs.getAllAsResult()

        assertTrue(result.isSuccess)
        val map = result.getOrThrow()
        assertEquals(2, map.size)
        assertEquals("1", map["a"])
        assertEquals(2, map["b"])
    }

    @Test
    fun givenEmptyKvs_whenGetAllAsResult_thenSuccessWithEmptyMap() = runTest {
        val kvs = kvs("ext_get_all_empty")

        val result = kvs.getAllAsResult()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun givenEditorWithChanges_whenApply_thenSuccessTrueAndChangesCommitted() = runTest {
        val kvs = kvs("ext_editor_apply")
        val applyResult = kvs.edit().putString("k", "v").apply()

        assertTrue(applyResult.isSuccess)
        assertTrue(applyResult.getOrThrow())
        assertEquals("v", kvs.getString("k", "def"))
    }

    @Test
    fun givenEditorClear_whenApply_thenSuccessAndStorageCleared() = runTest {
        val kvs = kvs("ext_editor_apply_clear")
        kvs.edit().putString("x", "y").commit()
        val applyResult = kvs.edit().clear().apply()

        assertTrue(applyResult.isSuccess)
        assertTrue(kvs.getAll().isEmpty())
    }
}
