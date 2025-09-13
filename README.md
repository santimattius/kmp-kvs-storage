
# KvsStorage

## Overview

KvsStorage is a Kotlin Multiplatform library that provides a simple, type-safe key-value storage solution. It's designed to work across multiple platforms including Android, iOS, and other Kotlin Multiplatform targets.

The library offers:
- Type-safe storage for common data types (String, Int, Long, Float, Boolean)
- Thread-safe operations with coroutine support
- Atomic updates through the editor pattern
- Clean and intuitive API

## Setup

### Prerequisites
- Kotlin 2.2.10 or higher
- Gradle 8.13.0 or higher
- Android Gradle Plugin 8.12.2 or higher (for Android projects)

### Installation

Add the following to your shared module's `build.gradle.kts`:

```kotlin
val kvsVersion = "1.0.0" // Check for the latest version

sourceSets {
    val commonMain by getting {
        dependencies {
            implementation("io.github.santimattius:kvs:$kvsVersion")
        }
    }
}
```

## Usage

### Initialization

#### Android
```kotlin
// In your Application class or dependency injection module
private val kvs = Storage.kvs("user_preferences")
```

#### iOS (Swift)
```swift
// In your AppDelegate or dependency injection setup
private let kvs = Storage.shared.kvs(name: "user_preferences")
```

### Basic Operations

#### Storing Data
```kotlin
// Using the editor pattern for multiple operations
kvs.edit()
    .putString("username", "john_doe")
    .putInt("user_age", 30)
    .putBoolean("is_premium", true)
    .commit() // Commits all changes atomically
```

#### Retrieving Data
```kotlin
// Launch in a coroutine scope
lifecycleScope.launch {
    val username = kvs.getString("username", "default_user")
    val age = kvs.getInt("user_age", 0)
    val isPremium = kvs.getBoolean("is_premium", false)
    
    // Get all stored values
    val allValues = kvs.getAll()
}
```

#### Removing Data
```kotlin
// Remove a single key
kvs.edit()
    .remove("username")
    .commit()

// Clear all data
kvs.edit()
    .clear()
    .commit()
```

### Advanced Usage

#### In-Memory Storage for Testing

For testing purposes, you can use `InMemoryKvs` which provides a volatile, in-memory implementation of the KVS interface. This is particularly useful for unit tests where you don't want to persist any data between test runs.

```kotlin
// In your test class (commonTest)
private val testKvs = Storage.inMemoryKvs("test_preferences")

@Test
fun `test kvs operations`() = runTest {
    // Store data
    testKvs.edit()
        .putString("test_key", "test_value")
        .commit()
    
    // Retrieve data
    val value = testKvs.getString("test_key", "default")
    assertEquals("test_value", value)
    
    // Clear data between tests
    testKvs.edit().clear().commit()
}

// On iOS (Swift)
// private let testKvs = Storage.shared.inMemoryKvs(name: "test_preferences")
```

#### Checking for Key Existence
```kotlin
lifecycleScope.launch {
    val hasUsername = kvs.contains("username")
}
```

#### Batch Operations
```kotlin
kvs.edit().apply {
    // Multiple operations
    putString("key1", "value1")
    putInt("key2", 42)
    remove("old_key")
}.commit() // All operations are atomic
```

## Best Practices

1. **Thread Safety**: All operations are thread-safe, but be mindful of the coroutine context when performing operations.

2. **Memory Management**: The storage is persistent, but avoid storing large amounts of data as it's not designed for large datasets.

3. **Error Handling**: Wrap storage operations in try-catch blocks when appropriate, especially when dealing with critical data.

4. **Key Naming**: Use a consistent naming convention for keys (e.g., `user_pref_username`, `app_settings_theme`).

5. **Performance**: For multiple operations, use the editor pattern to batch changes into a single atomic operation.

## Platform-Specific Notes

### Android & iOS
- Both platforms use `DataStore` under the hood for modern, type-safe storage
- Provides a consistent API across platforms with the same behavior and guarantees
- Handles data persistence and thread-safety automatically
- Backed by a file on the device's internal storage

## License

```
Copyright 2025 Santiago Mattiauda

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

