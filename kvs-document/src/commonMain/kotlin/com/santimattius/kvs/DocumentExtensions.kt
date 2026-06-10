package com.santimattius.kvs

import kotlinx.serialization.json.Json
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
suspend inline fun <reified T> Document.get(): T? {
    val content = read()
    if (content.isBlank()) return null
    return Json.decodeFromString<T>(content)
}

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
suspend inline fun <reified T> Document.put(value: T) {
    write(Json.encodeToString(value))
}
