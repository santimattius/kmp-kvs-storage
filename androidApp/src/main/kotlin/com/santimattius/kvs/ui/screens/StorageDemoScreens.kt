package com.santimattius.kvs.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.santimattius.kvs.Storage
import com.santimattius.kvs.document
import com.santimattius.kvs.encryptDocument
import com.santimattius.kvs.kvsLight
import com.santimattius.kvs.kvsLightEncrypt
import com.santimattius.kvs.kvsOptimized
import com.santimattius.kvs.viewmodel.DocumentDemoViewModel
import com.santimattius.kvs.viewmodel.EncryptDocumentDemoViewModel
import com.santimattius.kvs.viewmodel.InMemoryDemoViewModel
import com.santimattius.kvs.viewmodel.KvsLightDemoViewModel
import com.santimattius.kvs.viewmodel.KvsLightEncryptDemoViewModel
import com.santimattius.kvs.viewmodel.KvsOptimizedDemoViewModel

/**
 * One thin screen wrapper per [Storage] factory. Each wires the real backend-specific
 * instance into its `StorageDemoViewModel` (constructor injection) and renders the
 * shared [DemoScreen] layout.
 */
@Composable
fun InMemoryScreen(modifier: Modifier = Modifier) {
    val viewModel: InMemoryDemoViewModel = viewModel(
        factory = viewModelFactory {
            initializer { InMemoryDemoViewModel(Storage.inMemoryKvs("in_memory_demo")) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    DemoScreen(
        title = "In-Memory Kvs",
        state = state,
        onInputChange = viewModel::onInputChange,
        onPut = viewModel::put,
        onGet = viewModel::get,
        modifier = modifier,
    )
}

@Composable
fun KvsLightScreen(modifier: Modifier = Modifier) {
    val viewModel: KvsLightDemoViewModel = viewModel(
        factory = viewModelFactory {
            initializer { KvsLightDemoViewModel(Storage.kvsLight("kvs_light_demo")) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    DemoScreen(
        title = "Kvs Light",
        state = state,
        onInputChange = viewModel::onInputChange,
        onPut = viewModel::put,
        onGet = viewModel::get,
        modifier = modifier,
    )
}

@Composable
fun KvsLightEncryptScreen(modifier: Modifier = Modifier) {
    val viewModel: KvsLightEncryptDemoViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                KvsLightEncryptDemoViewModel(
                    Storage.kvsLightEncrypt("kvs_light_encrypt_demo", "demo_secret")
                )
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    DemoScreen(
        title = "Kvs Light (Encrypted)",
        state = state,
        onInputChange = viewModel::onInputChange,
        onPut = viewModel::put,
        onGet = viewModel::get,
        modifier = modifier,
    )
}

@Composable
fun KvsOptimizedScreen(modifier: Modifier = Modifier) {
    val viewModel: KvsOptimizedDemoViewModel = viewModel(
        factory = viewModelFactory {
            initializer { KvsOptimizedDemoViewModel(Storage.kvsOptimized("kvs_optimized_demo")) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    DemoScreen(
        title = "Kvs Optimized",
        state = state,
        onInputChange = viewModel::onInputChange,
        onPut = viewModel::put,
        onGet = viewModel::get,
        modifier = modifier,
    )
}

@Composable
fun DocumentScreen(modifier: Modifier = Modifier) {
    val viewModel: DocumentDemoViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DocumentDemoViewModel(Storage.document("document_demo")) }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    DemoScreen(
        title = "Document",
        state = state,
        onInputChange = viewModel::onInputChange,
        onPut = viewModel::put,
        onGet = viewModel::get,
        modifier = modifier,
    )
}

@Composable
fun EncryptDocumentScreen(modifier: Modifier = Modifier) {
    val viewModel: EncryptDocumentDemoViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                EncryptDocumentDemoViewModel(
                    Storage.encryptDocument("encrypt_document_demo", "demo_secret")
                )
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    DemoScreen(
        title = "Document (Encrypted)",
        state = state,
        onInputChange = viewModel::onInputChange,
        onPut = viewModel::put,
        onGet = viewModel::get,
        modifier = modifier,
    )
}
