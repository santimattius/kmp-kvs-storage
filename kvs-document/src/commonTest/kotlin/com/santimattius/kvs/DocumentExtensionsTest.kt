package com.santimattius.kvs

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DocumentExtensionsTest {

    @Serializable
    private data class User(val name: String, val age: Int)

    @Test
    fun givenEmptyDocument_whenGetCalled_thenReturnsNull() = runTest {
        val document = InMemoryDocument()
        val result = document.get<User>()
        assertNull(result)
    }

    @Test
    fun givenDocument_whenPutAndGetUser_thenReturnsSameValue() = runTest {
        val document = InMemoryDocument()
        val user = User("Santiago", 30)
        document.put(user)
        val result = document.get<User>()
        assertEquals(user, result)
    }

    @Test
    fun givenDocument_whenPutString_thenGetReturnsString() = runTest {
        val document = InMemoryDocument()
        document.put("hello world")
        val result = document.get<String>()
        assertEquals("hello world", result)
    }

    @Test
    fun givenDocument_whenPutInt_thenGetReturnsInt() = runTest {
        val document = InMemoryDocument()
        document.put(42)
        val result = document.get<Int>()
        assertEquals(42, result)
    }

    @Test
    fun givenDocument_whenPutList_thenGetReturnsList() = runTest {
        val document = InMemoryDocument()
        val items = listOf("a", "b", "c")
        document.put(items)
        val result = document.get<List<String>>()
        assertEquals(items, result)
    }

    @Test
    fun givenDocument_whenPutTwice_thenGetReturnsLastValue() = runTest {
        val document = InMemoryDocument()
        document.put(User("First", 25))
        document.put(User("Second", 35))
        val result = document.get<User>()
        assertEquals(User("Second", 35), result)
    }
}
