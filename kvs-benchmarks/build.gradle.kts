plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.santimattius.kvs.benchmarks"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"

        // Microbenchmark refuses to run on a debuggable build and/or an emulator by
        // default, because neither is representative of real-user timing. This module
        // has no non-debuggable "benchmark" build type configured yet (see PR-E
        // deviations), so CI/dev-machine runs on an emulator would otherwise always fail
        // with a hard error rather than just a warning. Suppressing lets the suite run
        // (and its correctness assertions be verified) locally/on CI emulators; anyone
        // reading real perf numbers off this module MUST re-run on a physical,
        // non-debuggable device — suppressed errors intentionally compromise accuracy.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] =
            "EMULATOR,DEBUGGABLE,LOW-BATTERY,UNLOCKED"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// This module is dev-only tooling: it has no `commonMain`/`androidMain` production code,
// only `androidTest` (instrumented) benchmarks. It is intentionally excluded from
// `settings.gradle.kts` publishing wiring, from `kvs-bom` constraints, and does NOT apply
// `maven-publish`/`com.vanniktech.maven.publish` (AC-E1, AC-E6).
dependencies {
    androidTestImplementation(projects.kvsCore)
    androidTestImplementation(projects.kvsPersistenceLight)
    androidTestImplementation(projects.kvsPersistenceOptimized)
    androidTestImplementation(projects.kvsDocument)

    androidTestImplementation(libs.androidx.benchmark.junit4)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.kotlinx.serialization.json)
}
