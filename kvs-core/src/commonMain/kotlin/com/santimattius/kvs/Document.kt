package com.santimattius.kvs

interface Document {
    suspend fun read(): String
    suspend fun write(value: String)
}
