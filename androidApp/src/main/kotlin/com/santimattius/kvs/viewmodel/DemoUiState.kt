package com.santimattius.kvs.viewmodel

/**
 * Shared UI state for every per-backend demo screen.
 *
 * Each `StorageDemoViewModel` subclass exposes one [DemoUiState] describing the
 * current text input, the last successful read/write result, the loading flag,
 * and any error surfaced to the user.
 */
data class DemoUiState(
    val inputValue: String = "",
    val result: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
