package com.santimattius.kvs.internal.exception

/**
 * Exception thrown when an error occurs during a read operation from the Key-Value Store.
 * This can happen if the data is corrupted, cannot be deserialized, or if there's an
 * underlying I/O error while accessing the storage.
 *
 * @param message A detailed message explaining the reason for the exception.
 * @param cause The underlying cause of the exception, if any.
 */
class ReadKvsException(message: String, cause: Throwable? = null) : KvsException(message, cause)

/**
 * Exception thrown when a write operation on the key-value store fails.
 * This could happen due to various reasons such as I/O errors, serialization issues,
 * or lack of storage space.
 *
 * @param message A detailed message explaining the reason for the exception.
 * @param cause The underlying cause of the exception, if any.
 */
class WriteKvsException(message: String, cause: Throwable? = null) : KvsException(message, cause)

/**
 * Exception thrown when an error occurs during a key-value removal operation.
 *
 * @param message A detailed message explaining the reason for the exception.
 */
class RemoveKvsException(message: String) : KvsException(message)

/**
 * Exception thrown when an error occurs while attempting to clear all key-value pairs
 * from the storage.
 *
 * @param message A detailed message explaining the reason for the exception.
 */
class ClearKvsException(message: String) : KvsException(message)

/**
 * Exception thrown when an error occurs during a key existence check operation in the key-value store.
 * This might happen if the underlying storage mechanism fails to report whether a key is present.
 *
 * @param message A detailed message explaining the reason for the exception.
 */
class ContainsKvsException(message: String) : KvsException(message)

/**
 * Exception thrown when an error occurs while attempting to retrieve all key-value pairs from the storage.
 *
 * @param message A detailed message explaining the reason for the exception.
 */
class GetAllKvsException(message: String) : KvsException(message)

/**
 * Base exception for any failures related to the Key-Value Store (KVS) operations.
 *
 * This abstract class serves as the superclass for more specific KVS exceptions,
 * such as [ReadKvsException], [WriteKvsException], etc. It provides a consistent
 * way to handle errors originating from the KVS system.
 *
 * @param message A descriptive message explaining the reason for the exception.
 * @param cause The underlying cause of the exception, if any.
 */
abstract class KvsException(message: String, cause: Throwable? = null) : Throwable(message, cause)