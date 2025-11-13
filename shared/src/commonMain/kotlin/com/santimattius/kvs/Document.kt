package com.santimattius.kvs

import kotlinx.serialization.json.Json
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * Represents a document that can be read from and written to.
 */
interface Document {
    /**
     * Reads the content of the document.
     * @return The content of the document as a string.
     */
    suspend fun read(): String

    /**
     * Writes the given value to the document.
     * @param value The value to write to the document.
     */
    suspend fun write(value: String)
}


@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
suspend inline fun <reified T> Document.get(): T? {
    return Json.decodeFromString<T>(read())
}

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
suspend inline fun <reified T> Document.put(value: T) {
    write(Json.encodeToString(value))
}