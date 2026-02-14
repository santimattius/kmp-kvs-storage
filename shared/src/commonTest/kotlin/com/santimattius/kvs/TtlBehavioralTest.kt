@file:OptIn(com.santimattius.kvs.ExperimentalKvsTtl::class)

package com.santimattius.kvs

import com.santimattius.kvs.internal.ttl.InMemoryTtlDataStore
import com.santimattius.kvs.internal.ttl.TTLEntity
import com.santimattius.kvs.internal.ttl.TtlManager
import com.santimattius.kvs.internal.ttl.extended.TtlKvsExtended
import com.santimattius.kvs.internal.ttl.cleanup.TtlCleanupJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Performance and behavioral tests for the TTL (Time-To-Live) feature.
 * Structured with **Given-When-Then** pattern:
 * - **Given** – preconditions and setup
 * - **When** – the action under test
 * - **Then** – expected outcome and assertions
 *
 * Uses in-memory DataStore and pre-populated expired entities (expiresAt in past) for
 * deterministic tests; real-time delay is used only where expiration over time is required.
 */
class TtlBehavioralTest {

    private fun createTtlKvs(
        dataStore: InMemoryTtlDataStore = InMemoryTtlDataStore(),
        defaultTtl: Duration? = 10.seconds
    ): TtlKvsExtended {
        val ttlManager = TtlManager(defaultTtl = defaultTtl)
        return TtlKvsExtended(dataStore = dataStore, ttlManager = ttlManager)
    }

    /** Past timestamp so that [TtlManager.isExpired] is true with [Clock.System]. */
    private fun expiredAt(): Long = 0L

    /** Far-future timestamp so key is never expired in tests. */
    private fun neverExpiresAt(): Long = Long.MAX_VALUE

    // --- Expiration behavior ---

    @Test
    fun getReturnsValueWhenNotExpired() = runTest {
        // Given: storage with default TTL and a key written (not yet expired)
        val store = InMemoryTtlDataStore()
        val kvs = createTtlKvs(store, defaultTtl = 10.seconds)
        kvs.edit().putString("key", "value").commit()

        // When: we get the value for that key
        val result = kvs.getString("key", "default")

        // Then: we get the stored value
        assertEquals("value", result)
    }

    @Test
    fun getReturnsDefValueWhenExpired() = runTest {
        // Given: storage containing a key that is already expired
        val store = InMemoryTtlDataStore(
            mapOf("key" to TTLEntity("key", "value", expiresAt = expiredAt()))
        )
        val kvs = createTtlKvs(store, defaultTtl = 10.seconds)

        // When: we get the value for that key
        val result = kvs.getString("key", "default")

        // Then: we get the default value (expired keys are treated as missing)
        assertEquals("default", result)
    }

    @Test
    fun expiredKeyStillInStorageUntilBatchCleanup() = runTest {
        // Given: storage with one expired key (no getAll or cleanup has run yet)
        val store = InMemoryTtlDataStore(
            mapOf("a" to TTLEntity("a", "1", expiresAt = expiredAt()))
        )
        val kvs = createTtlKvs(store, defaultTtl = 1.seconds)

        // When: we get the expired key (returns default)
        val getResult = kvs.getString("a", "def")

        // Then: get returns default and key is still in underlying store (no per-key cleanup)
        assertEquals("def", getResult)
        assertEquals(1, store.data.value.size)

        // When: we call getAll()
        val all = kvs.getAll()

        // Then: getAll() filters expired keys and performs batch cleanup
        assertTrue(all.isEmpty())
        assertEquals(0, store.data.value.size)
    }

    @Test
    fun containsReturnsFalseForExpiredKey() = runTest {
        // Given: storage with an expired key
        val store = InMemoryTtlDataStore(
            mapOf("k" to TTLEntity("k", "v", expiresAt = expiredAt()))
        )
        val kvs = createTtlKvs(store, defaultTtl = 1.seconds)

        // When: we check if the key is contained
        val result = kvs.contains("k")

        // Then: expired keys are considered not present
        assertFalse(result)
    }

    @Test
    fun containsReturnsFalseForMissingKey() = runTest {
        // Given: empty storage
        val kvs = createTtlKvs(InMemoryTtlDataStore())

        // When: we check for a key that was never stored
        val result = kvs.contains("missing")

        // Then: result is false
        assertFalse(result)
    }

    @Test
    fun containsReturnsTrueForNonExpiredKey() = runTest {
        // Given: storage with a non-expired key
        val kvs = createTtlKvs(InMemoryTtlDataStore())
        kvs.edit().putString("k", "v").commit()

        // When: we check if the key is contained
        val result = kvs.contains("k")

        // Then: result is true
        assertTrue(result)
    }

    // --- getAll batch cleanup ---

    @Test
    fun getAllFiltersExpiredKeysAndPerformsBatchCleanup() = runTest {
        // Given: storage with one non-expired key, one expired key, and we add another non-expired key
        val store = InMemoryTtlDataStore(
            mapOf(
                "live" to TTLEntity("live", "1", expiresAt = neverExpiresAt()),
                "expired" to TTLEntity("expired", "2", expiresAt = expiredAt())
            )
        )
        val kvs = createTtlKvs(store, defaultTtl = 5.seconds)
        kvs.edit().putString("live2", "3").commit()

        // When: we call getAll()
        val all = kvs.getAll()

        // Then: only non-expired keys are returned and persisted; expired key is removed in batch
        assertEquals(2, all.size)
        assertEquals("1", all["live"])
        assertEquals("3", all["live2"])
        assertNull(all["expired"])
        assertEquals(2, store.data.value.size)
    }

    @Test
    fun getAllOnEmptyStorageReturnsEmptyMap() = runTest {
        // Given: empty storage
        val kvs = createTtlKvs(InMemoryTtlDataStore())

        // When: we call getAll()
        val all = kvs.getAll()

        // Then: result is an empty map
        assertTrue(all.isEmpty())
    }

    // --- Per-key TTL override (real time: runBlocking so Clock.System advances) ---

    @Test
    fun perKeyDurationOverridesDefaultTtl() = runBlocking {
        // Given: storage with default TTL 60s; one key with 1s TTL, one with 100s TTL
        val kvs = createTtlKvs(InMemoryTtlDataStore(), defaultTtl = 60.seconds)
        kvs.edit()
            .putString("short", "s", 1.seconds)
            .putString("long", "l", 100.seconds)
            .commit()

        // When: we wait 2.1 seconds and then get both keys
        delay(2100)
        val shortResult = kvs.getString("short", "def")
        val longResult = kvs.getString("long", "def")

        // Then: short TTL key returns default; long TTL key returns stored value
        assertEquals("def", shortResult)
        assertEquals("l", longResult)
    }

    @Test
    fun defaultTtlUsedWhenNoPerKeyDuration() = runBlocking {
        // Given: storage with default TTL 2s and one key stored without per-key duration
        val kvs = createTtlKvs(InMemoryTtlDataStore(), defaultTtl = 2.seconds)
        kvs.edit().putString("k", "v").commit()

        // When: we get the key after 1s, then after 2.5s more
        delay(1000)
        val beforeExpiry = kvs.getString("k", "def")
        delay(2500)
        val afterExpiry = kvs.getString("k", "def")

        // Then: before expiry we get the value; after expiry we get the default
        assertEquals("v", beforeExpiry)
        assertEquals("def", afterExpiry)
    }

    // --- No default TTL (expiresAt null) ---

    @Test
    fun keyWithoutTtlNeverExpiresWhenNoDefaultTtl() = runTest {
        // Given: storage with no default TTL and a key stored (no expiration)
        val kvs = createTtlKvs(InMemoryTtlDataStore(), defaultTtl = null)
        kvs.edit().putString("k", "v").commit()

        // When: we get the key
        val result = kvs.getString("k", "def")

        // Then: we always get the stored value (key never expires)
        assertEquals("v", result)
    }

    // --- All value types ---

    @Test
    fun allValueTypesRoundTrip() = runTest {
        // Given: storage with values of each type stored
        val kvs = createTtlKvs(InMemoryTtlDataStore())
        kvs.edit()
            .putString("s", "hello")
            .putInt("i", 42)
            .putLong("l", 123L)
            .putFloat("f", 3.14f)
            .putBoolean("b", true)
            .commit()

        // When: we read each key back
        val s = kvs.getString("s", "")
        val i = kvs.getInt("i", 0)
        val l = kvs.getLong("l", 0L)
        val f = kvs.getFloat("f", 0f)
        val b = kvs.getBoolean("b", false)

        // Then: each value matches what was stored
        assertEquals("hello", s)
        assertEquals(42, i)
        assertEquals(123L, l)
        assertEquals(3.14f, f)
        assertTrue(b)
    }

    // --- CleanupJob behavior ---

    @Test
    fun cleanupJobRemovesExpiredKeys() = runBlocking {
        // Given: storage with two expired keys and a running cleanup job (short interval)
        val store = InMemoryTtlDataStore(
            mapOf(
                "a" to TTLEntity("a", "1", expiresAt = expiredAt()),
                "b" to TTLEntity("b", "2", expiresAt = expiredAt())
            )
        )
        val ttlManager = TtlManager(defaultTtl = 1.seconds)
        val job = TtlCleanupJob(store, ttlManager, 1.milliseconds)
        val cleanupJob = job.start(this)

        // When: we wait for at least one cleanup run, then cancel the job
        delay(150)
        cleanupJob.cancel()

        // Then: expired keys have been removed from storage
        assertEquals(0, store.data.value.size)
    }

    // --- Performance / behavioral: batch-only cleanup ---

    @Test
    fun multipleGetOrDefaultForExpiredKeyDoesNotGrowWrites() = runTest {
        // Given: storage with one expired key (batch cleanup not run)
        val store = InMemoryTtlDataStore(
            mapOf("k" to TTLEntity("k", "v", expiresAt = expiredAt()))
        )
        val kvs = createTtlKvs(store, defaultTtl = 1.seconds)

        // When: we get the expired key multiple times (each returns default)
        repeat(10) {
            kvs.getString("k", "def")
        }

        // Then: no per-key removal happened; key still in store (batch-only cleanup)
        assertEquals(1, store.data.value.size)
    }

    @Test
    fun getAllWithManyKeysCompletesInReasonableTime() = runTest {
        // Given: storage with 500 keys
        val store = InMemoryTtlDataStore()
        val kvs = createTtlKvs(store, defaultTtl = 60.seconds)
        kvs.edit().apply {
            repeat(500) { i -> putString("key_$i", "value_$i") }
            commit()
        }

        // When: we call getAll() and measure elapsed time
        val start = kotlin.time.TimeSource.Monotonic.markNow()
        val all = kvs.getAll()
        val elapsed = start.elapsedNow()

        // Then: getAll completes within 5 seconds and returns all keys
        assertTrue(elapsed < 5.seconds)
        assertEquals(500, all.size)
    }

    @Test
    fun getAllWithManyExpiredKeysCleansInSingleBatch() = runTest {
        // Given: storage with 200 expired keys
        val initial = buildMap {
            repeat(200) { i -> put("key_$i", TTLEntity("key_$i", "v$i", expiresAt = expiredAt())) }
        }
        val store = InMemoryTtlDataStore(initial)
        val kvs = createTtlKvs(store, defaultTtl = 1.seconds)

        // When: we call getAll()
        val all = kvs.getAll()

        // Then: result is empty and all expired keys were removed in a single batch
        assertTrue(all.isEmpty())
        assertEquals(0, store.data.value.size)
    }

    // --- Flow behavior ---

    @Test
    fun getAllAsStreamEmitsOnlyNonExpired() = runTest {
        // Given: storage with one non-expired and one expired key
        val store = InMemoryTtlDataStore(
            mapOf(
                "a" to TTLEntity("a", "1", expiresAt = neverExpiresAt()),
                "b" to TTLEntity("b", "2", expiresAt = expiredAt())
            )
        )
        val kvs = createTtlKvs(store, defaultTtl = 10.seconds)

        // When: we collect the first emission of getAllAsStream()
        val map = kvs.getAllAsStream().first()

        // Then: only the non-expired key is present
        assertEquals(1, map.size)
        assertEquals("1", map["a"])
    }

    @Test
    fun getStringAsStreamEmitsDefValueWhenExpired() = runTest {
        // Given: storage with an expired key
        val store = InMemoryTtlDataStore(
            mapOf("k" to TTLEntity("k", "v", expiresAt = expiredAt()))
        )
        val kvs = createTtlKvs(store, defaultTtl = 1.seconds)

        // When: we collect the first emission of getStringAsStream for that key
        val result = kvs.getStringAsStream("k", "def").first()

        // Then: we get the default value
        assertEquals("def", result)
    }

    // --- Edge: clear and remove ---

    @Test
    fun clearRemovesAllKeys() = runTest {
        // Given: storage with two keys
        val kvs = createTtlKvs(InMemoryTtlDataStore())
        kvs.edit().putString("a", "1").putString("b", "2").commit()

        // When: we clear the storage and then get one of the keys
        kvs.edit().clear().commit()
        val all = kvs.getAll()
        val aResult = kvs.getString("a", "def")

        // Then: getAll is empty and get returns default
        assertTrue(all.isEmpty())
        assertEquals("def", aResult)
    }

    @Test
    fun removeKeyThenGetReturnsDefValue() = runTest {
        // Given: storage with one key
        val kvs = createTtlKvs(InMemoryTtlDataStore())
        kvs.edit().putString("k", "v").commit()

        // When: we remove the key and then get it
        kvs.edit().remove("k").commit()
        val result = kvs.getString("k", "def")

        // Then: we get the default value
        assertEquals("def", result)
    }

    // --- Edge: single key, empty after cleanup ---

    @Test
    fun singleKeyExpiredReturnsDefValue() = runTest {
        // Given: storage with a single expired key
        val store = InMemoryTtlDataStore(
            mapOf("only" to TTLEntity("only", "x", expiresAt = expiredAt()))
        )
        val kvs = createTtlKvs(store)

        // When: we get the key and then call getAll()
        val getResult = kvs.getString("only", "def")
        val all = kvs.getAll()

        // Then: get returns default and getAll is empty (cleanup removed the key)
        assertEquals("def", getResult)
        assertTrue(all.isEmpty())
    }
}
