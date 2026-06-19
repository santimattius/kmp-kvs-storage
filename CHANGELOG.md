# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Breaking changes

- `KvsStandard`, `KvsStream`, and `TtlManager` are now annotated with `@InternalKvsApi`. They are cross-module implementation details and are not part of the public API. Any direct usage now requires `@OptIn(InternalKvsApi::class)` and should be considered unsupported.
- `lightPersistencePath()` and `lightEncryptor()` in `kvs-persistence-light` are now annotated with `@InternalKvsApi`. These functions are bridges between library modules only and were never intended for external consumers. If you called them directly, migrate to the public `Storage.document()` and `Storage.encryptDocument()` APIs, or open an issue.
- `AppContextInitializer` moved from `com.santimattius.kvs.internal.context` to `com.santimattius.kvs.android`. If you reference this class directly in your own manifest (non-standard use), update the `android:name` attribute.
- `OptimizedKvsInitializer` moved from `com.santimattius.kvs.internal` to `com.santimattius.kvs.android`. Same note applies.

## [2.0.0] — 2026-06-07

### Breaking changes

**Artifact split.** The monolithic `io.github.santimattius:kvs` artifact is replaced by focused modules:

| Module | Maven coordinate | Purpose |
|--------|------------------|---------|
| `kvs-core` | `io.github.santimattius:kvs-core:2.0.0` | API + in-memory KVS |
| `kvs-persistence-light` | `io.github.santimattius:kvs-persistence-light:2.0.0` | Light persistence (KVS, TTL, encryption) |
| `kvs-document` | `io.github.santimattius:kvs-document:2.0.0` | Single-document storage + JSON serialization |
| `kvs-persistence-optimized` | `io.github.santimattius:kvs-persistence-optimized:2.0.0` | Optimized persistence for large datasets |

**Aggregator artifact.** `io.github.santimattius:kvs:2.0.0` remains available as a convenience bundle that depends on all modules above. Individual artifacts are recommended for smaller binaries.

**BOM (optional).** `io.github.santimattius:kvs-bom:2.0.0` aligns versions of all artifacts. Use `implementation(platform("io.github.santimattius:kvs-bom:2.0.0"))` and omit versions on individual coordinates. The BOM is optional and adds no runtime dependencies.

**API factory methods.** Canonical persistence factories are now explicit:

| Use case | 2.0 API | Module |
|----------|---------|--------|
| Light KVS | `Storage.kvsLight(name)` | `kvs-persistence-light` |
| Light encrypted KVS | `Storage.kvsLightEncrypt(name, secretKey)` | `kvs-persistence-light` |
| Light KVS + TTL | `Storage.kvsLight(name, ttl = …)` | `kvs-persistence-light` |
| Optimized KVS | `Storage.kvsOptimized(name)` | `kvs-persistence-optimized` |
| Document | `Storage.document(name)` | `kvs-document` |
| Encrypted document | `Storage.encryptDocument(name, secretKey)` | `kvs-document` |
| In-memory | `Storage.inMemoryKvs(name)` | `kvs-core` |

Deprecated aliases (`simpleKvs`, `optimizedKvs`, `kvs(name)` without TTL) remain for migration but will be removed in a future release.

**Backend selection.** When both light and optimized backends are on the classpath, call `kvsLight` or `kvsOptimized` explicitly — there is no automatic resolution.

### Migration

See [MIGRATION.md](MIGRATION.md) for step-by-step upgrade from 1.x.

### Monolith decision (ADR)

The former `shared` module implementation was removed. `shared` is now a thin aggregator (Option B) that re-exports the four published modules and provides the iOS `KvsStorage` XCFramework.

---

## [1.3.0] and earlier

See [GitHub Releases](https://github.com/santimattius/kmp-kvs-storage/releases) for 1.x history.
