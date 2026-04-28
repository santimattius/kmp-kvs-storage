package com.santimattius.kvs

import kotlinx.serialization.json.Json

suspend inline fun <reified T> Document.get(): T? {
    val content = read()
    if (content.isEmpty()) return null
    return Json.decodeFromString<T>(content)
}

suspend inline fun <reified T> Document.put(value: T) {
    write(Json.encodeToString(value))
}
