package com.santimattius.kvs.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.santimattius.kvs.navigation.AppRoutes

/** Entry point listing all 6 [com.santimattius.kvs.Storage] factory demos. */
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Kvs Storage Demos", style = MaterialTheme.typography.headlineSmall)

        Button(onClick = { onNavigate(AppRoutes.IN_MEMORY) }) { Text("In-Memory Kvs") }
        Button(onClick = { onNavigate(AppRoutes.KVS_LIGHT) }) { Text("Kvs Light") }
        Button(onClick = { onNavigate(AppRoutes.KVS_LIGHT_ENCRYPT) }) { Text("Kvs Light (Encrypted)") }
        Button(onClick = { onNavigate(AppRoutes.KVS_OPTIMIZED) }) { Text("Kvs Optimized") }
        Button(onClick = { onNavigate(AppRoutes.DOCUMENT) }) { Text("Document") }
        Button(onClick = { onNavigate(AppRoutes.ENCRYPT_DOCUMENT) }) { Text("Document (Encrypted)") }
    }
}
