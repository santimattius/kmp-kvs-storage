package com.santimattius.kvs.viewmodel

import com.santimattius.kvs.Document

/** Demo ViewModel for the [com.santimattius.kvs.encryptDocument] backend. */
class EncryptDocumentDemoViewModel(private val document: Document) : StorageDemoViewModel() {

    override suspend fun performPut(value: String) {
        document.write(value)
    }

    override suspend fun performGet(): String = document.read()
}
