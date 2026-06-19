package com.santimattius.kvs

import kotlin.test.Test
import kotlin.test.assertNotNull

class InternalKvsApiAnnotationTest {
    @Test
    fun annotationClassExists() {
        // Verifies InternalKvsApi is declared and accessible
        val annotation = InternalKvsApi::class
        assertNotNull(annotation)
    }
}
