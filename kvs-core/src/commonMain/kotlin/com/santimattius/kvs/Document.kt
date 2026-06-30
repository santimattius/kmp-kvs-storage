package com.santimattius.kvs

import kotlinx.coroutines.CancellationException

/**
 * Contract for a single serialized document stored as a raw [String].
 *
 * Implementations handle the underlying persistence (e.g. DataStore JSON).
 * The [read] and [write] operations are suspending so they can be safely
 * called from any coroutine without blocking the calling thread.
 *
 * Obtain an instance via `Storage.document(...)` from the `kvs-document` artifact.
 */
interface Document {

    /**
     * Reads the current content of the document.
     *
     * Returns an empty string if no content has been written yet.
     *
     * @return The stored document content, or an empty string if absent.
     * @throws CancellationException if the calling coroutine is cancelled before the read
     *   completes. Structured concurrency is respected — the exception propagates normally.
     */
    @Throws(CancellationException::class)
    suspend fun read(): String

    /**
     * Overwrites the document with [value].
     *
     * The write is atomic from the perspective of concurrent readers — they
     * will observe either the previous or the new value, never a partial write.
     *
     * @param value The content to persist. Pass an empty string to clear the document.
     * @throws CancellationException if the calling coroutine is cancelled before the write
     *   completes. Structured concurrency is respected — the exception propagates normally.
     */
    @Throws(CancellationException::class)
    suspend fun write(value: String)
}
