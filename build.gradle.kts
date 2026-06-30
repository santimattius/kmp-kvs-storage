plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKMPLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.swiftklib) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.dokka)
}

dependencies {
    // Dokka V2 multi-module aggregation: each publishable module contributes
    // its commonMain-rendered KDoc to a single unified API reference site
    // generated at the root via `./gradlew dokkaGenerate`.
    dokka(project(":kvs-core"))
    dokka(project(":kvs-persistence-light"))
    dokka(project(":kvs-persistence-optimized"))
    dokka(project(":kvs-document"))
}