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
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Duration.Companion.hours

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
            Log.d("MainViewModel", "read: ${kvs.getString("token", "error")}")
            updateDocument()
            tempKey(UUID.randomUUID().toString())
        }
    }

    fun save(value: Boolean) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            val token = UUID.randomUUID().toString()
            Log.d("MainViewModel", "token: $token")
            kvs.edit()
                .putBoolean("dark_mode", value)
                .putString("name", "Santiago")
                .putString("surname", "Mattiauda")
                .putString("token", token)
                .apply().onSuccess {
                    Log.d("MainViewModel", "save: $it")
                }.onFailure {
                    Log.d("MainViewModel", "save: $it")
                }
        }
    }

    fun updateDocument() {
        viewModelScope.launch(Dispatchers.IO) {
            val document = Storage.document("profile")
            //val document = Storage.encryptDocument("profile", "secret")

            val userProfile = Profile(
                username = "santimattius",
                email = "email@example.com"
            )
            document.put(userProfile)
            val result: Profile? = document.get()
            Log.d("MainViewModel", "updateDocument: $result")
        }
    }

    fun tempKey(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val kvsTemp = Storage.kvs("secure", encrypted = true)
            kvsTemp.edit()
                .putString("name", "Santiago") // without TTL
                .putString(key = "token", value = token, duration = 1.hours) // with TTL
                .commit()
        }
    }

    @Serializable
    data class Profile(val username: String, val email: String)

}