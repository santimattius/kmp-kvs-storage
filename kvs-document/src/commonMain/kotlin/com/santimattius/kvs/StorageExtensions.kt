package com.santimattius.kvs

import com.santimattius.kvs.internal.datastore.encrypt.Encryptor
import com.santimattius.kvs.internal.document.DataStoreDocument
import com.santimattius.kvs.internal.document.provideDocumentDataStoreInstance

fun Storage.document(name: String): Document =
    DataStoreDocument(provideDocumentDataStoreInstance(name, Encryptor.None))

fun Storage.encryptDocument(name: String, secretKey: String): Document =
    DataStoreDocument(provideDocumentDataStoreInstance(name, lightEncryptor(secretKey)))
