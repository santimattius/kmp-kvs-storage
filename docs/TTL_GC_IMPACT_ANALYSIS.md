# TTL Implementation – GC and Performance Impact (Kotlin Multiplatform)

## Summary

The TTL implementation **has been optimized** for performance across all Kotlin Multiplatform targets (Android, iOS, etc.). The main causes of unnecessary allocations and GC pressure have been addressed with batch-only cleanup, single-pass collections, background dispatcher, and Flow coalescing. On **Android**, this reduces GC spikes and UI jank; on **iOS/Native**, the same patterns reduce allocations and keep heavy I/O off the main thread.

---

## Implemented Mitigations

### 1. Batch-only cleanup (high impact) — DONE

**Problem:** Each expired key read triggered a full map copy (`updateData`), causing frequent GC.

**Solution:** Per-key lazy cleanup was **removed** from `getOrDefault()`. When a key is expired we only return `defValue`; we do **not** call `updateData` to remove that key. Cleanup happens only in:
- **`getAll()`** — batch removal of all expired keys before returning.
- **`CleanupJob`** — periodic batch cleanup on a background dispatcher.

**Files:** `TtlKvsExtendedStandard.kt`  
**Result:** No full map copy on single-key read; fewer allocations on all targets (and fewer GC spikes on Android).

---

### 2. Single-pass collections with `buildMap` / `buildSet` — DONE

**Problem:** Chained `.filter { }.keys` and `.filter { }.mapValues { }` created multiple intermediate maps/sets per call.

**Solution:**
- **`getAll()`:** Uses `buildSet` for expired keys and `buildMap` for the result (single pass each).
- **`getAllAsStream()`:** Uses single `buildMap { }` per emission.
- **`TtlCleanupJob.cleanupExpiredKeys()`:** Uses `buildSet` for expired keys.

**Files:** `TtlKvsExtendedStandard.kt`, `TtlKvsExtendedStream.kt`, `TtlCleanupJob.kt`  
**Result:** Fewer temporary collections and less allocation pressure on all platforms.

---

### 3. Cleanup job on background dispatcher — DONE

**Problem:** Cleanup and I/O on the main/UI thread could cause jank (Android) or block the main thread (iOS).

**Solution:** `TtlCleanupJob.start()` launches the loop with **`Dispatchers.IO`** so all cleanup work (read, filter, write) runs off the main thread on every target.

**Files:** `TtlCleanupJob.kt`  
**Result:** Main thread stays responsive on all platforms; on Android, GC from cleanup is less likely to cause UI stutter.

---

### 4. Flow: `distinctUntilChanged()` — DONE

**Problem:** Every `ds.data` emission produced a new map in `getAllAsStream()` and new values in single-key flows, even when the filtered result was unchanged.

**Solution:** All TTL Flow APIs use **`distinctUntilChanged()`** after the `map`:
- **`getAllAsStream()`** — same filtered map is not re-emitted.
- **`getOrDefault()` (Flow)** — same value is not re-emitted.

**Files:** `TtlKvsExtendedStream.kt`  
**Result:** Fewer allocations and downstream work when DataStore emits repeatedly with unchanged content.

---

### 5. Cleanup job runs for any expired keys — DONE

**Problem:** The previous threshold (cleanup only when > 100 expired keys) could leave many expired keys on disk and delay cleanup.

**Solution:** Cleanup now runs whenever **there is at least one expired key**. The job still runs at the configured **interval**, so I/O remains batched in time and allocation/GC impact is controlled on all targets.

**Files:** `TtlCleanupJob.kt`  
**Result:** Storage stays lean without increasing the number of cleanup runs (still one batch per interval).

---

## Remaining Constraints (DataStore API)

These cannot be improved without changing the underlying storage abstraction:

1. **Full map read on every single-key read**  
   DataStore exposes `data: Flow<Map<...>>` and no “get one key” API. So `getString(key)` etc. still use `ds.data.first()` and load the full map. The improvement is that we **no longer** do a full map **write** (updateData) on expired key read.

2. **Full map copy on every `updateData`**  
   DataStore’s `updateData` is “read full state → transform → write full state”. So each cleanup (in `getAll()` or in the job) does one full map copy. We only trigger cleanup in batch (getAll or periodic job), so the number of full copies is minimized.

---

## Usage Recommendations (all targets)

1. **Use the CleanupJob for TTL storage**  
   Start a cleanup job so expired keys are removed in the background without blocking the main/UI thread:
   ```kotlin
   val kvs = Storage.kvs("cache", ttl = myTtl)
   kvs.cleanupJob(Duration.ofMinutes(10)).start(applicationScope)
   ```

2. **Prefer `getAll()` when you need many keys**  
   One `getAll()` does one read and one batch cleanup; multiple `getString()` calls do one full read each (no cleanup per key anymore). This applies on all platforms.

3. **Collect Flow on a background dispatcher**  
   If you collect `getAllAsStream()` or single-key streams on the UI thread, use `flowOn(Dispatchers.IO)` or collect in a scope that uses a background dispatcher so that the map building and equality check in `distinctUntilChanged` don’t run on the main thread. On Android this avoids jank; on iOS it keeps the main thread free.

---

## Conclusion

The TTL layer is now **optimized for performance across Kotlin Multiplatform targets** by:

- **Batch-only cleanup** — no per-key `updateData` on read.
- **Single-pass collections** — `buildMap` / `buildSet` instead of chained filters.
- **Background cleanup** — `CleanupJob` runs on `Dispatchers.IO` on all platforms.
- **Fewer Flow emissions** — `distinctUntilChanged()` to avoid redundant maps/values.
- **Periodic batch cleanup** — cleanup runs for any expired keys at each interval.

Remaining allocation cost is largely from DataStore’s full-map read/write model; the above changes minimize how often we trigger writes and how much we allocate per read/emission. These benefits apply to both Android (GC and UI responsiveness) and iOS/Native (allocations and main-thread load).

---

## Is this the best implementation for performance?

**Short answer:** For the **current design** (DataStore + in-process TTL), **yes** — this is a strong, performance-oriented implementation. We’ve minimized allocations, GC/main-thread pressure, and heavy work on the main thread within that design, on all KMP targets.

**What “best” means here:**

| Aspect | Status | Note |
|--------|--------|------|
| **Write path (cleanup)** | Optimal | Batch-only cleanup; no per-key `updateData`. Minimal number of full map copies. |
| **Collection building** | Optimal | Single-pass `buildMap` / `buildSet`; no chained intermediate collections. |
| **Background work** | Optimal | Cleanup runs on `Dispatchers.IO` on all targets; no main-thread I/O or heavy allocation. |
| **Flow emissions** | Optimal | `distinctUntilChanged()` avoids redundant downstream work and duplicate emissions. |
| **Read path (single key)** | Constrained | Every `getString(key)` still does `ds.data.first()` (full map). Limited by DataStore API, not by our code. |

So **within the DataStore + TTL abstraction**, we’re doing the best we can for performance and resource use on all platforms.

**Could performance be better with a different design?**

Yes, but that would be a **different architecture**, not a tweak of this one:

1. **Storage with get-by-key**  
   A backend that supports “read one key” (e.g. SQLite, or a cache with key-based access) would avoid loading the full map on every single-key read. That would reduce allocations and I/O for read-heavy, single-key usage. DataStore doesn’t offer that; changing it would mean a different storage layer.

2. **App-level read cache**  
   An in-memory cache (e.g. last snapshot + short TTL) could make repeated reads faster and reduce calls to `ds.data.first()`. Trade-offs: more memory, cache invalidation, and possible staleness. It’s a feature with non-trivial complexity and isn’t required for “best implementation **within** the current design.”

3. **Native TTL storage**  
   Some stores (e.g. Redis, or DBs with TTL) handle expiration internally and can avoid full map scans. Again, that’s a different backend, not an evolution of the current one.

**Conclusion:** For **this** implementation (DataStore-backed, in-process TTL), the current code is the best balance of performance and simplicity. Further gains would require a different storage or an explicit read cache, with corresponding trade-offs in complexity and behavior.
