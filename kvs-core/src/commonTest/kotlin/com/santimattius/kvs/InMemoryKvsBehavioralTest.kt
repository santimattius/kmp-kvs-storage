package com.santimattius.kvs

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryKvsBehavioralTest {

    // --- Storage.inMemoryKvs factory ---

    @Test
    fun inMemoryKvsReturnsSameInstanceForSameName() {
        // Given two calls with the same name
        val a = Storage.inMemoryKvs("same")
        val b = Storage.inMemoryKvs("same")
        // Then they are the same instance
        assertTrue(a === b)
    }

    @Test
    fun inMemoryKvsReturnsDifferentInstancesForDifferentNames() {
        // Given two calls with different names
        val a = Storage.inMemoryKvs("alpha")
        val b = Storage.inMemoryKvs("beta")
        // Then they are distinct instances
        assertFalse(a === b)
    }

    // --- put / get round-trips ---

    @Test
    fun putStringAndGetStringReturnsStoredValue() = runTest {
        // Given an empty KVS
        val kvs = Storage.inMemoryKvs("put-string")
        // When we put a string and commit
        kvs.edit().putString("key", "hello").commit()
        // Then getString returns the stored value
        assertEquals("hello", kvs.getString("key", "default"))
    }

    @Test
    fun putIntAndGetIntReturnsStoredValue() = runTest {
        val kvs = Storage.inMemoryKvs("put-int")
        kvs.edit().putInt("count", 42).commit()
        assertEquals(42, kvs.getInt("count", 0))
    }

    @Test
    fun putLongAndGetLongReturnsStoredValue() = runTest {
        val kvs = Storage.inMemoryKvs("put-long")
        kvs.edit().putLong("ts", 123456789L).commit()
        assertEquals(123456789L, kvs.getLong("ts", 0L))
    }

    @Test
    fun putFloatAndGetFloatReturnsStoredValue() = runTest {
        val kvs = Storage.inMemoryKvs("put-float")
        kvs.edit().putFloat("ratio", 3.14f).commit()
        assertEquals(3.14f, kvs.getFloat("ratio", 0f))
    }

    @Test
    fun putBooleanAndGetBooleanReturnsStoredValue() = runTest {
        val kvs = Storage.inMemoryKvs("put-bool")
        kvs.edit().putBoolean("flag", true).commit()
        assertTrue(kvs.getBoolean("flag", false))
    }

    // --- default values ---

    @Test
    fun getStringReturnsDefValueWhenKeyAbsent() = runTest {
        // Given an empty KVS
        val kvs = Storage.inMemoryKvs("get-default-string")
        // When we get a key that was never put
        val result = kvs.getString("missing", "fallback")
        // Then we get the default value
        assertEquals("fallback", result)
    }

    @Test
    fun getIntReturnsDefValueWhenKeyAbsent() = runTest {
        val kvs = Storage.inMemoryKvs("get-default-int")
        assertEquals(-1, kvs.getInt("missing", -1))
    }

    // --- contains ---

    @Test
    fun containsReturnsFalseForAbsentKey() = runTest {
        val kvs = Storage.inMemoryKvs("contains-absent")
        assertFalse(kvs.contains("nope"))
    }

    @Test
    fun containsReturnsTrueAfterPut() = runTest {
        val kvs = Storage.inMemoryKvs("contains-present")
        kvs.edit().putString("k", "v").commit()
        assertTrue(kvs.contains("k"))
    }

    // --- getAll ---

    @Test
    fun getAllReturnsEmptyMapWhenNoKeysStored() = runTest {
        val kvs = Storage.inMemoryKvs("getall-empty")
        assertTrue(kvs.getAll().isEmpty())
    }

    @Test
    fun getAllReturnsAllStoredEntries() = runTest {
        val kvs = Storage.inMemoryKvs("getall-entries")
        kvs.edit().putString("a", "1").putInt("b", 2).commit()
        val all = kvs.getAll()
        assertEquals(2, all.size)
        assertEquals("1", all["a"])
        assertEquals(2, all["b"])
    }

    // --- remove ---

    @Test
    fun removeKeyThenGetReturnsDefValue() = runTest {
        val kvs = Storage.inMemoryKvs("remove-key")
        kvs.edit().putString("k", "v").commit()
        kvs.edit().remove("k").commit()
        assertEquals("gone", kvs.getString("k", "gone"))
        assertFalse(kvs.contains("k"))
    }

    // --- clear ---

    @Test
    fun clearRemovesAllStoredKeys() = runTest {
        val kvs = Storage.inMemoryKvs("clear-all")
        kvs.edit().putString("a", "1").putString("b", "2").commit()
        kvs.edit().clear().commit()
        assertTrue(kvs.getAll().isEmpty())
    }

    // --- atomic commit: uncommitted edits not visible ---

    @Test
    fun editorChangesNotVisibleBeforeCommit() = runTest {
        val kvs = Storage.inMemoryKvs("atomic-commit")
        val editor = kvs.edit().putString("staged", "value")
        // Before commit, key is absent
        assertFalse(kvs.contains("staged"))
        editor.commit()
        // After commit, key is present
        assertTrue(kvs.contains("staged"))
    }

    // --- streams ---

    @Test
    fun getAllAsStreamEmitsCurrentState() = runTest {
        val kvs = Storage.inMemoryKvs("stream-getall")
        kvs.edit().putString("x", "y").commit()
        val snapshot = kvs.getAllAsStream().first()
        assertEquals("y", snapshot["x"])
    }

    @Test
    fun getStringAsStreamEmitsStoredValue() = runTest {
        val kvs = Storage.inMemoryKvs("stream-string")
        kvs.edit().putString("name", "KVS").commit()
        val value = kvs.getStringAsStream("name", "").first()
        assertEquals("KVS", value)
    }
}
