# Migration Guide: KvsStorage 1.x → 2.0

KvsStorage 2.0 splits the single `kvs` artifact into four focused modules. This is a **breaking change** in the dependency structure. The public API (method names, types, behaviour) stays the same; only the Gradle coordinates change.

---

## 1. Dependency changes

### 1.x (single artifact)

```kotlin
// build.gradle.kts
implementation("io.github.santimattius:kvs:1.3.x")
```

### 2.0 (pick the modules you need)

```kotlin
// Minimum — in-memory KVS only (no disk persistence)
implementation("io.github.santimattius:kvs-core:2.0.0")

// Light persistence: KVS, encrypted KVS, TTL
implementation("io.github.santimattius:kvs-persistence-light:2.0.0")

// Single-document storage + typed JSON serialization (Document.get<T> / Document.put<T>)
implementation("io.github.santimattius:kvs-document:2.0.0")

// Optimized persistence: large datasets, efficient TTL
implementation("io.github.santimattius:kvs-persistence-optimized:2.0.0")
```

Typical combination for existing 1.x users who used the monolithic `kvs` artifact with disk persistence:

```kotlin
implementation("io.github.santimattius:kvs-core:2.0.0")
implementation("io.github.santimattius:kvs-persistence-light:2.0.0")
implementation("io.github.santimattius:kvs-document:2.0.0")  // only if you use Document
```

---

## 2. API migration by use case

### In-memory KVS (no disk persistence)

No code changes needed. Requires only `kvs-core`.

```kotlin
// 1.x
val kvs = Storage.inMemoryKvs("name")

// 2.0 (identical call, now from kvs-core)
val kvs = Storage.inMemoryKvs("name")
```

### Persistent KVS — light backend (no TTL)

Requires `kvs-core` + `kvs-persistence-light`.

```kotlin
// 1.x
val kvs = Storage.kvs("preferences")

// 2.0 — canonical API
val kvs = Storage.kvsLight("preferences")
```

### Encrypted KVS

Requires `kvs-core` + `kvs-persistence-light`.

```kotlin
// 1.x
val kvs = Storage.encryptKvs("secure", "my-secret-key")

// 2.0 — canonical API
val kvs = Storage.kvsLightEncrypt("secure", "my-secret-key")
```

### KVS with TTL

Requires `kvs-core` + `kvs-persistence-light`.

```kotlin
// 1.x
@OptIn(ExperimentalKvsTtl::class)
val cache: KvsExtended = Storage.kvs("cache", ttl = 1.hours)

// 2.0 — canonical API
@OptIn(ExperimentalKvsTtl::class)
val cache: KvsExtended = Storage.kvsLight("cache", ttl = 1.hours)
```

### Document storage

Requires `kvs-core` + `kvs-persistence-light` + `kvs-document`.

```kotlin
// 1.x
val doc = Storage.document("profile")
val encDoc = Storage.encryptDocument("profile", "secret-key")

// 2.0 (identical calls)
val doc = Storage.document("profile")
val encDoc = Storage.encryptDocument("profile", "secret-key")
```

### Typed Document serialization (`Document.get<T>` / `Document.put<T>`)

Requires `kvs-core` + `kvs-persistence-light` + `kvs-document`.

```kotlin
@Serializable
data class UserProfile(val name: String, val email: String)

val doc = Storage.document("profile")

// 2.0 — same calls, now provided by kvs-document
doc.put(UserProfile("Santiago", "s@example.com"))
val profile: UserProfile? = doc.get()
```

### High-throughput / large-dataset KVS (new in 2.0)

Requires `kvs-core` + `kvs-persistence-optimized`.

```kotlin
// Simple
val kvs: Kvs = Storage.kvsOptimized("large-cache")

// With TTL
@OptIn(ExperimentalKvsTtl::class)
val cache: KvsExtended = Storage.kvsOptimized("large-cache", ttl = 24.hours)
```

---

## 3. Debug logging

```kotlin
// 1.x
Storage.debug(true)

// 2.0 (identical call, now in kvs-core)
Storage.debug(true)
```

---

## 4. Aggregator vs individual artifacts

In 2.0, `io.github.santimattius:kvs:2.0.0` is a **convenience aggregator** that pulls in all modules. It replaces the old monolithic implementation but does not duplicate source code.

For smaller binaries, depend on individual artifacts (`kvs-core`, `kvs-persistence-light`, etc.) instead of the aggregator.

The 1.x monolith (`kvs:1.x`) is no longer maintained. Use the migration steps above to move to 2.0.

---

## 5. Choose modules by use case, not by implementation

2.0 does not expose persistence technologies in its public API. Choose modules based on your use case:

| Need | Module |
|------|--------|
| Testing / no-persistence | `kvs-core` |
| Typical app storage (small–medium data) | `kvs-core` + `kvs-persistence-light` |
| Document / object storage | + `kvs-document` |
| High-throughput / large-dataset / efficient TTL | `kvs-core` + `kvs-persistence-optimized` |

---

## 6. BOM (optional)

To align versions without repeating `2.0.0` on every line:

```kotlin
implementation(platform("io.github.santimattius:kvs-bom:2.0.0"))
implementation("io.github.santimattius:kvs-core")
implementation("io.github.santimattius:kvs-persistence-light")
```

The BOM is optional. Projects that prioritize a small dependency footprint should keep explicit versions and include only the artifacts they need.

---

## 7. Choosing light vs optimized backend

2.0 uses **explicit factory methods** (Option C). There is no automatic backend resolution when both artifacts are on the classpath.

| Backend | Factory methods | Artifact |
|---------|-----------------|----------|
| Light | `Storage.kvsLight(...)`, `Storage.kvsLightEncrypt(...)` | `kvs-persistence-light` |
| Optimized | `Storage.kvsOptimized(...)` | `kvs-persistence-optimized` |

**Both backends on classpath:** call the method that matches the backend you intend. For example, use `kvsLight` for app preferences and `kvsOptimized` for a large TTL cache in the same project.

Deprecated aliases (`simpleKvs`, `optimizedKvs`, `kvs(name)` without TTL) remain available but will be removed in a future release.
