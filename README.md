
# [ALPHA] KVS Storage
The KVS library provides a Key-Value storage system with the following interfaces:

### `Kvs` Interface

This interface provides methods for reading, writing, and managing key-value pairs.

*   `suspend fun getAll(): Map<String, Any>`: Retrieves all stored key-value pairs.
*   `suspend fun getString(key: String, defValue: String): String`: Retrieves a String value.
*   `suspend fun getInt(key: String, defValue: Int): Int`: Retrieves an Int value.
*   `suspend fun getLong(key: String, defValue: Long): Long`: Retrieves a Long value.
*   `suspend fun getFloat(key: String, defValue: Float): Float`: Retrieves a Float value.
*   `suspend fun getBoolean(key: String, defValue: Boolean): Boolean`: Retrieves a Boolean value.
*   `fun edit(): KvsEditor`: Returns a `KvsEditor` instance to modify values.
*   `suspend operator fun contains(key: String): Boolean`: Checks if a key exists.

### `Kvs.KvsEditor` Interface

An editor for modifying values in storage. Changes are applied atomically when `commit()` is called.

*   `fun putString(key: String, value: String): KvsEditor`: Sets a String value.
*   `fun putInt(key: String, value: Int): KvsEditor`: Sets an Int value.
*   `fun putLong(key: String, value: Long): KvsEditor`: Sets a Long value.
*   `fun putFloat(key: String, value: Float): KvsEditor`: Sets a Float value.
*   `fun putBoolean(key: String, value: Boolean): KvsEditor`: Sets a Boolean value.
*   `fun remove(key: String): KvsEditor`: Removes a value.
*   `fun clear(): KvsEditor`: Removes all values.
*   `suspend fun commit()`: Applies changes asynchronously.
