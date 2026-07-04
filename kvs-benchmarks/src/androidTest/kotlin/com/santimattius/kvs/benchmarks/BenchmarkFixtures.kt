package com.santimattius.kvs.benchmarks

import com.santimattius.kvs.Kvs

/**
 * Writes [count] string entries into [kvs] as a single atomic commit.
 *
 * Shared by every backend's `getAll` benchmark ([InMemoryBenchmark], [LightKvsBenchmark],
 * [OptimizedKvsBenchmark]) so all three measure an identical 100-entry seed shape (AC-E3).
 */
internal suspend fun seed(kvs: Kvs, count: Int) {
    var editor = kvs.edit()
    repeat(count) { index ->
        editor = editor.putString("key-$index", "value-$index")
    }
    editor.commit()
}
