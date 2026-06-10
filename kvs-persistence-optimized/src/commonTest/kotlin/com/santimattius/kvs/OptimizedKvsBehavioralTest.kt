@file:OptIn(com.santimattius.kvs.ExperimentalKvsTtl::class)

package com.santimattius.kvs

import com.santimattius.kvs.internal.OptimizedKvs
import com.santimattius.kvs.internal.OptimizedKvsExtended
import com.santimattius.kvs.internal.TtlBatchCleanupJob
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.persistence.optimized.db.KvsDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class OptimizedKvsBehavioralTest {

    private lateinit var db: KvsDatabase
    private lateinit var kvs: OptimizedKvs
    private lateinit var ttlKvs: OptimizedKvsExtended

    @BeforeTest
    fun setUp() {
        db = KvsDatabase(createTestSqlDriver())
        kvs = OptimizedKvs(db.kvsEntryQueries)
        ttlKvs = OptimizedKvsExtended(
            queries = db.kvsEntryQueries,
            ttlManager = TtlManager(defaultTtl = 10.seconds)
        )
    }

    @AfterTest
    fun tearDown() {
        db.kvsEntryQueries.deleteAll()
    }

    // --- OptimizedKvs: basic get/put ---

    @Test
    fun givenEmptyStore_whenGetString_thenReturnsDefValue() = runTest {
        // Given: empty store
        // When: we get a key that does not exist
        val result = kvs.getString("missing", "default")
        // Then: the default value is returned
        assertEquals("default", result)
    }

    @Test
    fun givenStoredString_whenGetString_thenReturnsValue() = runTest {
        // Given: a string stored under a key
        kvs.edit().putString("k", "hello").commit()
        // When: we get that key
        val result = kvs.getString("k", "def")
        // Then: the stored value is returned
        assertEquals("hello", result)
    }

    @Test
    fun givenStoredInt_whenGetInt_thenReturnsValue() = runTest {
        // Given: an int stored under a key
        kvs.edit().putInt("i", 42).commit()
        // When: we get that key as int
        val result = kvs.getInt("i", 0)
        // Then: the stored value is returned
        assertEquals(42, result)
    }

    @Test
    fun givenStoredLong_whenGetLong_thenReturnsValue() = runTest {
        // Given: a long stored under a key
        kvs.edit().putLong("l", 123L).commit()
        // When: we get that key as long
        val result = kvs.getLong("l", 0L)
        // Then: the stored value is returned
        assertEquals(123L, result)
    }

    @Test
    fun givenStoredFloat_whenGetFloat_thenReturnsValue() = runTest {
        // Given: a float stored under a key
        kvs.edit().putFloat("f", 3.14f).commit()
        // When: we get that key as float
        val result = kvs.getFloat("f", 0f)
        // Then: the stored value is returned
        assertEquals(3.14f, result)
    }

    @Test
    fun givenStoredBoolean_whenGetBoolean_thenReturnsValue() = runTest {
        // Given: a boolean stored under a key
        kvs.edit().putBoolean("b", true).commit()
        // When: we get that key as boolean
        val result = kvs.getBoolean("b", false)
        // Then: the stored value is returned
        assertTrue(result)
    }

    @Test
    fun givenMultipleKeys_whenGetAll_thenReturnsAllEntries() = runTest {
        // Given: several keys stored
        kvs.edit().putString("a", "1").putString("b", "2").putString("c", "3").commit()
        // When: we call getAll
        val all = kvs.getAll()
        // Then: all entries are returned
        assertEquals(3, all.size)
        assertEquals("1", all["a"])
        assertEquals("2", all["b"])
        assertEquals("3", all["c"])
    }

    @Test
    fun givenStoredKey_whenContains_thenReturnsTrue() = runTest {
        // Given: a key stored
        kvs.edit().putString("present", "v").commit()
        // When: we check contains
        val result = kvs.contains("present")
        // Then: returns true
        assertTrue(result)
    }

    @Test
    fun givenMissingKey_whenContains_thenReturnsFalse() = runTest {
        // Given: empty store
        // When: we check contains for a key that doesn't exist
        val result = kvs.contains("absent")
        // Then: returns false
        assertFalse(result)
    }

    @Test
    fun givenStoredKey_whenRemoved_thenGetReturnsDefValue() = runTest {
        // Given: a key stored, then removed
        kvs.edit().putString("k", "v").commit()
        kvs.edit().remove("k").commit()
        // When: we get the removed key
        val result = kvs.getString("k", "default")
        // Then: the default value is returned
        assertEquals("default", result)
    }

    @Test
    fun givenMultipleKeys_whenCleared_thenGetAllReturnsEmpty() = runTest {
        // Given: multiple keys stored, then cleared
        kvs.edit().putString("a", "1").putString("b", "2").commit()
        kvs.edit().clear().commit()
        // When: we call getAll
        val all = kvs.getAll()
        // Then: the store is empty
        assertTrue(all.isEmpty())
    }

    @Test
    fun givenStoredKey_whenGetAllAsStream_thenEmitsEntry() = runTest {
        // Given: a key stored
        kvs.edit().putString("s", "val").commit()
        // When: we take the first emission from getAllAsStream
        val result = kvs.getAllAsStream().first()
        // Then: the entry appears in the stream
        assertEquals("val", result["s"])
    }

    @Test
    fun givenStoredKey_whenGetStringAsStream_thenEmitsValue() = runTest {
        // Given: a key stored
        kvs.edit().putString("sk", "stream_val").commit()
        // When: we take the first emission from getStringAsStream
        val result = kvs.getStringAsStream("sk", "def").first()
        // Then: the stored value is emitted
        assertEquals("stream_val", result)
    }

    // --- OptimizedKvsExtended: TTL ---

    @Test
    fun givenExpiredKey_whenGetString_thenReturnsDefValue() = runTest {
        // Given: a key stored with TTL that has already passed
        val queries = db.kvsEntryQueries
        queries.upsert("expired", "v", 0L)  // expires_at = epoch (already expired)
        // When: we get the key via the TTL-aware implementation
        val result = ttlKvs.getString("expired", "default")
        // Then: the default value is returned (key is expired)
        assertEquals("default", result)
    }

    @Test
    fun givenNonExpiredKey_whenGetString_thenReturnsValue() = runTest {
        // Given: a key stored with TTL in the future
        val futureExpiry = kotlin.time.Clock.System.now().toEpochMilliseconds() + 60_000L
        db.kvsEntryQueries.upsert("live", "hello", futureExpiry)
        // When: we get the key via the TTL-aware implementation
        val result = ttlKvs.getString("live", "default")
        // Then: the stored value is returned
        assertEquals("hello", result)
    }

    @Test
    fun givenExpiredKey_whenContains_thenReturnsFalse() = runTest {
        // Given: a key that is already expired
        db.kvsEntryQueries.upsert("exp", "v", 0L)
        // When: we check contains via the TTL-aware implementation
        val result = ttlKvs.contains("exp")
        // Then: returns false (expired = not present)
        assertFalse(result)
    }

    @Test
    fun givenExpiredKeys_whenDeleteExpired_thenRowsRemoved() = runTest {
        // Given: two expired keys and one live key
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        db.kvsEntryQueries.upsert("dead1", "v1", 0L)
        db.kvsEntryQueries.upsert("dead2", "v2", 0L)
        db.kvsEntryQueries.upsert("live", "v3", now + 60_000L)
        // When: deleteExpired is called with current time
        db.kvsEntryQueries.deleteExpired(now)
        // Then: only the live key remains
        val all = db.kvsEntryQueries.selectAll().executeAsList()
        assertEquals(1, all.size)
        assertEquals("live", all.first().key)
    }

    @Test
    fun givenExpiredKeys_whenCleanupJobRuns_thenExpiredRowsRemoved() = runBlocking {
        // Given: two expired keys in the store
        db.kvsEntryQueries.upsert("a", "1", 0L)
        db.kvsEntryQueries.upsert("b", "2", 0L)
        val cleanupJob = TtlBatchCleanupJob(db.kvsEntryQueries, 1.milliseconds)
        // When: cleanup job runs briefly then is cancelled
        val job = cleanupJob.start(this)
        delay(150)
        job.cancel()
        // Then: expired keys have been removed
        val remaining = db.kvsEntryQueries.selectAll().executeAsList()
        assertEquals(0, remaining.size)
    }

    @Test
    fun givenManyKeys_whenGetAll_thenReturnsAllInReasonableTime() = runTest {
        // Given: 500 keys stored
        kvs.edit().apply {
            repeat(500) { i -> putString("key_$i", "value_$i") }
            commit()
        }
        // When: we call getAll and measure time
        val start = kotlin.time.TimeSource.Monotonic.markNow()
        val all = kvs.getAll()
        val elapsed = start.elapsedNow()
        // Then: all 500 entries are returned within 5 seconds
        assertEquals(500, all.size)
        assertTrue(elapsed < 5.seconds)
    }
}
