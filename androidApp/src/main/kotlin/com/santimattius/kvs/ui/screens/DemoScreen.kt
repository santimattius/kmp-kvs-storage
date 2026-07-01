package com.santimattius.kvs.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.santimattius.kvs.viewmodel.DemoUiState

/**
 * Shared layout used by every per-backend demo screen: a text field for the
 * value, Put/Get actions, and a result/error surface bound to [state].
 */
@Composable
fun DemoScreen(
    title: String,
    state: DemoUiState,
    onInputChange: (String) -> Unit,
    onPut: () -> Unit,
    onGet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.inputValue,
            onValueChange = onInputChange,
            label = { Text("Value") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        Row {
            Button(onClick = onPut, enabled = !state.isLoading) { Text("Put") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onGet, enabled = !state.isLoading) { Text("Get") }
        }
        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        }
        state.result?.let { result ->
            Text(text = "Result: $result")
        }
        state.error?.let { error ->
            Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
        }
    }
}
