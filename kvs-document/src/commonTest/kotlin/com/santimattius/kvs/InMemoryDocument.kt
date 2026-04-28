package com.santimattius.kvs

internal class InMemoryDocument(initial: String = "") : Document {
    private var content = initial
    override suspend fun read(): String = content
    override suspend fun write(value: String) { content = value }
}
