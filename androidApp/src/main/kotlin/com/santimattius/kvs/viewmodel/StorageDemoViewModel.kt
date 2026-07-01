package com.santimattius.kvs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel shared by all 6 backend demo screens.
 *
 * Subclasses implement [performPut] and [performGet] against the [com.santimattius.kvs.Kvs]
 * or [com.santimattius.kvs.Document] instance they were constructed with (constructor
 * injection keeps this class — and its subclasses — testable without an Android
 * `Context`, since fakes implementing the same contract can be substituted in tests).
 */
abstract class StorageDemoViewModel : ViewModel() {

    private val _state = MutableStateFlow(DemoUiState())
    val state: StateFlow<DemoUiState> = _state.asStateFlow()

    /** Updates the staged input value as the user types. */
    fun onInputChange(value: String) {
        _state.update { it.copy(inputValue = value) }
    }

    /** Writes the current [DemoUiState.inputValue] to the backend. */
    fun put() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { performPut(_state.value.inputValue) }
                .onSuccess {
                    _state.update { it.copy(isLoading = false, result = "Saved", error = null) }
                }
                .onFailure { error -> handleFailure(error) }
        }
    }

    /** Reads the current value back from the backend. */
    fun get() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { performGet() }
                .onSuccess { value ->
                    _state.update { it.copy(isLoading = false, result = value, error = null) }
                }
                .onFailure { error -> handleFailure(error) }
        }
    }

    private fun handleFailure(error: Throwable) {
        if (error is CancellationException) throw error
        _state.update { it.copy(isLoading = false, error = error.message ?: "Unknown error") }
    }

    protected abstract suspend fun performPut(value: String)
    protected abstract suspend fun performGet(): String
}
