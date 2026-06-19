package com.santimattius.kvs

import com.santimattius.kvs.Encryptor
import com.santimattius.kvs.internal.datastore.encrypt.encryptor
import com.santimattius.kvs.internal.logger.logger
import com.santimattius.kvs.internal.producePath

/**
 * Resolves the on-disk path for a file stored by the light persistence backend.
 * Intended for sibling artifacts (e.g. kvs-document) that build on light persistence.
 */
@InternalKvsApi
fun lightPersistencePath(fileName: String): String = producePath(fileName)

/**
 * Creates an encryptor backed by the light persistence encryption stack.
 */
@InternalKvsApi
fun lightEncryptor(secretKey: String): Encryptor = encryptor(key = secretKey, logger = logger())
