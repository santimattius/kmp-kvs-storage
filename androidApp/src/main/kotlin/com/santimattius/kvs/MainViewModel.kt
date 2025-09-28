package com.santimattius.kvs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val kvs = Storage.encryptKvs("user_preferences", "secret")
    private val inMemoryKvs = Storage.inMemoryKvs("user_preferences")

    private val _isDarkModeEnabled = MutableStateFlow(false)
    /*val isDarkModeEnabled: StateFlow<Boolean> = _isDarkModeEnabled.onStart {
        read()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(1000),
        initialValue = false
    )*/

    val isDarkModeEnabled: StateFlow<Boolean> = kvs.getBooleanAsStream(
        key = "dark_mode",
        defValue = false
    ).onStart {
        read()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(1000),
        initialValue = false
    )

    private var job: Job? = null

    private fun read() {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            Log.d("MainViewModel", "read: ${kvs.getString("name", "error")}")
        }
    }

    fun save(value: Boolean) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            kvs.edit()
                .putBoolean("dark_mode", value)
                .putString("name", "Santiago")
                .commit()
        }
    }

}