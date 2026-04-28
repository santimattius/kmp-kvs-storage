import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKMPLibrary)
    alias(libs.plugins.skie)
    alias(libs.plugins.swiftklib)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.santimattius"
version = "2.0.0-SNAPSHOT"

kotlin {
    androidLibrary {
        namespace = "io.github.santimattius.kvs.persistence.light"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.compilations {
            val main by getting {
                cinterops {
                    create("KtCrypto")
                }
            }
        }
        it.binaries.framework {
            baseName = "KvsPersistenceLight"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.kvsCore)
            implementation(libs.kotlin.stdlib)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.startup.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.datastore)
        }
    }
}

swiftklib {
    create("KtCrypto") {
        // TODO Phase 3: move to kvs-persistence-light/native/KtCrypto
        path = file("../shared/native/KtCrypto")
        packageName("com.santimattius.kvs.native")
    }
}

skie {
    isEnabled.set(true)
    swiftBundling {
        enabled = true
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "kvs-persistence-light", version.toString())
    pom {
        name = "KvsPersistenceLight"
        description = "KvsStorage lightweight persistence backend for key-value storage."
        inceptionYear = "2025"
        url = "https://github.com/santimattius/kmp-kvs-storage/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "santiago-mattiauda"
                name = "Santiago Mattiauda"
                url = "https://github.com/santimattius"
            }
        }
        scm {
            url = "https://github.com/santimattius/kmp-kvs-storage/"
            connection = "scm:git:git://github.com/santimattius/kmp-kvs-storage.git"
            developerConnection = "scm:git:ssh://git@github.com/santimattius/kmp-kvs-storage.git"
        }
    }
}
