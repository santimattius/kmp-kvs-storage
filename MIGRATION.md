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

// DataStore-backed KVS, encrypted KVS, TTL, Document storage
implementation("io.github.santimattius:kvs-persistence-light:2.0.0")

// Typed JSON serialization for Document (Document.get<T> / Document.put<T>)
implementation("io.github.santimattius:kvs-document:2.0.0")

// SQLite-backed KVS with efficient TTL for large datasets
implementation("io.github.santimattius:kvs-persistence-optimized:2.0.0")
```

Typical combination for existing 1.x users who used DataStore:

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

### DataStore-backed KVS (no TTL)

Requires `kvs-core` + `kvs-persistence-light`.

```kotlin
// 1.x
val kvs = Storage.simpleKvs("preferences")
// or the old alias:
val kvs = Storage.kvs("preferences")

// 2.0 (identical call, now an extension from kvs-persistence-light)
val kvs = Storage.simpleKvs("preferences")
```

### Encrypted KVS

Requires `kvs-core` + `kvs-persistence-light`.

```kotlin
// 1.x
val kvs = Storage.simpleEncryptKvs("secure", "my-secret-key")

// 2.0 (identical call)
val kvs = Storage.simpleEncryptKvs("secure", "my-secret-key")
```

### KVS with TTL

Requires `kvs-core` + `kvs-persistence-light`.

```kotlin
// 1.x
@OptIn(ExperimentalKvsTtl::class)
val cache: KvsExtended = Storage.kvs("cache", ttl = 1.hours)

// 2.0 (identical call)
@OptIn(ExperimentalKvsTtl::class)
val cache: KvsExtended = Storage.kvs("cache", ttl = 1.hours)
```

### Document storage

Requires `kvs-core` + `kvs-persistence-light`.

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

Requires `kvs-core` + `kvs-persistence-optimized`. Uses SQLite instead of DataStore.

```kotlin
// Simple
val kvs: Kvs = Storage.optimizedKvs("large-cache")

// With TTL
@OptIn(ExperimentalKvsTtl::class)
val cache: KvsExtended = Storage.optimizedKvs("large-cache", ttl = 24.hours)
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

## 4. Deprecated 1.x artifact

The `io.github.santimattius:kvs` artifact (the old `shared` module) is deprecated as of version `1.3.0-deprecated`. It continues to compile but every method emits a deprecation warning pointing to the 2.0 equivalent. It will be removed in a future release.

---

## 5. No DataStore / SQLDelight knowledge required

2.0 does not expose DataStore, Room, or SQLDelight in its public API. Choose modules based on your use case, not by the underlying technology:

| Need | Module |
|------|--------|
| Testing / no-persistence | `kvs-core` |
| Typical app storage (small–medium data) | `kvs-core` + `kvs-persistence-light` |
| Document / object storage | + `kvs-document` |
| High-throughput / large-dataset / efficient TTL | `kvs-core` + `kvs-persistence-optimized` |
