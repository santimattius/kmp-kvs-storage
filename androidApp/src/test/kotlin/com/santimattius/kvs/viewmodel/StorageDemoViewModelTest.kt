@file:OptIn(ExperimentalCoroutinesApi::class)

package com.santimattius.kvs.viewmodel

import com.santimattius.kvs.Document
import com.santimattius.kvs.Kvs
import com.santimattius.kvs.ReadKvsException
import com.santimattius.kvs.Storage
import com.santimattius.kvs.WriteKvsException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * RED phase fixtures for the 6 per-backend demo ViewModels.
 *
 * Every ViewModel under test is constructor-injected with a [Kvs] or [Document]
 * instance so the tests stay fast/deterministic and never need an Android
 * [android.content.Context] (the real `kvsLight`/`kvsLightEncrypt`/`kvsOptimized`
 * factories require one). [Storage.inMemoryKvs] is used as a stand-in backend
 * because it implements the very same [Kvs] contract the production screens use.
 */
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class ThrowingKvs(
    private val delegate: Kvs,
    private val failPut: Boolean = false,
    private val failGet: Boolean = false,
) : Kvs by delegate {

    override suspend fun getString(key: String, defValue: String): String {
        if (failGet) throw ReadKvsException("Simulated read failure")
        return delegate.getString(key, defValue)
    }

    override fun edit(): Kvs.KvsEditor {
        val editor = delegate.edit()
        return if (failPut) ThrowingEditor(editor) else editor
    }
}

private class ThrowingEditor(
    private val delegate: Kvs.KvsEditor,
) : Kvs.KvsEditor {
    // Note: each method below must return `this` (not the delegate's return value) so the
    // fluent chain `edit().putX(...).commit()` keeps resolving to this wrapper's `commit()`.
    // Plain `by delegate` forwarding would leak the unwrapped delegate from each call.
    override fun putString(key: String, value: String): Kvs.KvsEditor {
        delegate.putString(key, value)
        return this
    }

    override fun putInt(key: String, value: Int): Kvs.KvsEditor {
        delegate.putInt(key, value)
        return this
    }

    override fun putLong(key: String, value: Long): Kvs.KvsEditor {
        delegate.putLong(key, value)
        return this
    }

    override fun putFloat(key: String, value: Float): Kvs.KvsEditor {
        delegate.putFloat(key, value)
        return this
    }

    override fun putBoolean(key: String, value: Boolean): Kvs.KvsEditor {
        delegate.putBoolean(key, value)
        return this
    }

    override fun remove(key: String): Kvs.KvsEditor {
        delegate.remove(key)
        return this
    }

    override fun clear(): Kvs.KvsEditor {
        delegate.clear()
        return this
    }

    override suspend fun commit() {
        throw WriteKvsException("Simulated write failure")
    }
}

private class FakeDocument(initial: String = "") : Document {
    private var content = initial
    override suspend fun read(): String = content
    override suspend fun write(value: String) {
        content = value
    }
}

private class ThrowingDocument(
    private val delegate: Document,
    private val failPut: Boolean = false,
    private val failGet: Boolean = false,
) : Document by delegate {

    override suspend fun read(): String {
        if (failGet) throw ReadKvsException("Simulated read failure")
        return delegate.read()
    }

    override suspend fun write(value: String) {
        if (failPut) throw WriteKvsException("Simulated write failure")
        delegate.write(value)
    }
}

class InMemoryDemoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `put surfaces a success result`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = InMemoryDemoViewModel(Storage.inMemoryKvs("test_in_memory_put"))
        viewModel.onInputChange("hello")

        viewModel.put()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
    }

    @Test
    fun `get reflects the previously stored value`() = runTest(mainDispatcherRule.testDispatcher) {
        val kvs = Storage.inMemoryKvs("test_in_memory_get")
        val viewModel = InMemoryDemoViewModel(kvs)
        viewModel.onInputChange("world")
        viewModel.put()
        advanceUntilIdle()

        viewModel.get()
        advanceUntilIdle()

        assertEquals("world", viewModel.state.value.result)
    }

    @Test
    fun `failure surfaces an error state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = InMemoryDemoViewModel(
            ThrowingKvs(Storage.inMemoryKvs("test_in_memory_err"), failPut = true)
        )
        viewModel.onInputChange("oops")

        viewModel.put()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }
}

class KvsLightDemoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `put surfaces a success result`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = KvsLightDemoViewModel(Storage.inMemoryKvs("test_light_put"))
        viewModel.onInputChange("hello")

        viewModel.put()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
    }

    @Test
    fun `get reflects the previously stored value`() = runTest(mainDispatcherRule.testDispatcher) {
        val kvs = Storage.inMemoryKvs("test_light_get")
        val viewModel = KvsLightDemoViewModel(kvs)
        viewModel.onInputChange("world")
        viewModel.put()
        advanceUntilIdle()

        viewModel.get()
        advanceUntilIdle()

        assertEquals("world", viewModel.state.value.result)
    }

    @Test
    fun `failure surfaces an error state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = KvsLightDemoViewModel(
            ThrowingKvs(Storage.inMemoryKvs("test_light_err"), failGet = true)
        )

        viewModel.get()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }
}

class KvsLightEncryptDemoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `put surfaces a success result`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = KvsLightEncryptDemoViewModel(Storage.inMemoryKvs("test_light_encrypt_put"))
        viewModel.onInputChange("hello")

        viewModel.put()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
    }

    @Test
    fun `get reflects the previously stored value`() = runTest(mainDispatcherRule.testDispatcher) {
        val kvs = Storage.inMemoryKvs("test_light_encrypt_get")
        val viewModel = KvsLightEncryptDemoViewModel(kvs)
        viewModel.onInputChange("secret")
        viewModel.put()
        advanceUntilIdle()

        viewModel.get()
        advanceUntilIdle()

        assertEquals("secret", viewModel.state.value.result)
    }

    @Test
    fun `failure surfaces an error state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = KvsLightEncryptDemoViewModel(
            ThrowingKvs(Storage.inMemoryKvs("test_light_encrypt_err"), failPut = true)
        )
        viewModel.onInputChange("oops")

        viewModel.put()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }
}

class KvsOptimizedDemoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `put surfaces a success result`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = KvsOptimizedDemoViewModel(Storage.inMemoryKvs("test_optimized_put"))
        viewModel.onInputChange("hello")

        viewModel.put()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
    }

    @Test
    fun `get reflects the previously stored value`() = runTest(mainDispatcherRule.testDispatcher) {
        val kvs = Storage.inMemoryKvs("test_optimized_get")
        val viewModel = KvsOptimizedDemoViewModel(kvs)
        viewModel.onInputChange("world")
        viewModel.put()
        advanceUntilIdle()

        viewModel.get()
        advanceUntilIdle()

        assertEquals("world", viewModel.state.value.result)
    }

    @Test
    fun `failure surfaces an error state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = KvsOptimizedDemoViewModel(
            ThrowingKvs(Storage.inMemoryKvs("test_optimized_err"), failGet = true)
        )

        viewModel.get()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }
}

class DocumentDemoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `put surfaces a success result`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = DocumentDemoViewModel(FakeDocument())
        viewModel.onInputChange("hello")

        viewModel.put()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
    }

    @Test
    fun `get reflects the previously stored value`() = runTest(mainDispatcherRule.testDispatcher) {
        val document = FakeDocument()
        val viewModel = DocumentDemoViewModel(document)
        viewModel.onInputChange("world")
        viewModel.put()
        advanceUntilIdle()

        viewModel.get()
        advanceUntilIdle()

        assertEquals("world", viewModel.state.value.result)
    }

    @Test
    fun `failure surfaces an error state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = DocumentDemoViewModel(ThrowingDocument(FakeDocument(), failPut = true))
        viewModel.onInputChange("oops")

        viewModel.put()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }
}

class EncryptDocumentDemoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `put surfaces a success result`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = EncryptDocumentDemoViewModel(FakeDocument())
        viewModel.onInputChange("hello")

        viewModel.put()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.result)
    }

    @Test
    fun `get reflects the previously stored value`() = runTest(mainDispatcherRule.testDispatcher) {
        val document = FakeDocument()
        val viewModel = EncryptDocumentDemoViewModel(document)
        viewModel.onInputChange("secret")
        viewModel.put()
        advanceUntilIdle()

        viewModel.get()
        advanceUntilIdle()

        assertEquals("secret", viewModel.state.value.result)
    }

    @Test
    fun `failure surfaces an error state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = EncryptDocumentDemoViewModel(
            ThrowingDocument(FakeDocument(), failGet = true)
        )

        viewModel.get()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }
}
