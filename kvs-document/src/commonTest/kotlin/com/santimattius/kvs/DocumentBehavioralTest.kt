package com.santimattius.kvs

import com.santimattius.kvs.internal.document.DataStoreDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DocumentBehavioralTest {

    @Serializable
    private data class User(val name: String, val age: Int)

    @Test
    fun givenEmptyDocument_whenRead_thenReturnsEmptyString() = runTest {
        val document = DataStoreDocument(FakeStringDataStore())

        val result = document.read()

        assertEquals("", result)
    }

    @Test
    fun givenDocument_whenWriteAndRead_thenReturnsWrittenValue() = runTest {
        val document = DataStoreDocument(FakeStringDataStore())

        document.write("hello")
        val result = document.read()

        assertEquals("hello", result)
    }

    @Test
    fun givenEmptyDocument_whenGetTypedValue_thenReturnsNull() = runTest {
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
    fun givenDocument_whenOverwrite_thenReturnsLatestValue() = runTest {
        val document = InMemoryDocument()

        document.put(User("First", 1))
        document.put(User("Second", 2))
        val result = document.get<User>()

        assertEquals(User("Second", 2), result)
    }
}

private class FakeStringDataStore : androidx.datastore.core.DataStore<String> {
    private val state = MutableStateFlow("")

    override val data = state

    override suspend fun updateData(transform: suspend (String) -> String): String {
        var result = ""
        state.update { current ->
            result = transform(current)
            result
        }
        return result
    }
}
