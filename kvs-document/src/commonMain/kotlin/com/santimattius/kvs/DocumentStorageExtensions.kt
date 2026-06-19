package com.santimattius.kvs

import com.santimattius.kvs.internal.datastore.encrypt.Encryptor
import com.santimattius.kvs.internal.document.DataStoreDocument
import com.santimattius.kvs.internal.document.provideDocumentDataStoreInstance

/**
 * Returns a [Document] backed by light persistence.
 * Requires `kvs-core`, `kvs-persistence-light`, and `kvs-document` on the classpath.
 */
fun Storage.document(name: String): Document =
    DataStoreDocument(provideDocumentDataStoreInstance(name, Encryptor.None))

/**
 * Returns an encrypted [Document] backed by light persistence.
 * Requires `kvs-core`, `kvs-persistence-light`, and `kvs-document` on the classpath.
 */
fun Storage.encryptDocument(name: String, secretKey: String): Document =
    DataStoreDocument(provideDocumentDataStoreInstance(name, lightEncryptor(secretKey)))
