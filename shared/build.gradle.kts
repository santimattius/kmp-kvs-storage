import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKMPLibrary)
    alias(libs.plugins.kotlinAtomicfu)
    alias(libs.plugins.skie)
}

kotlin {
    androidLibrary {
        namespace = "com.santimattius.kvs.shared"
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
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.startup.runtime)
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            // DataStore library
            implementation(libs.androidx.datastore)
            // The Preferences DataStore library
            implementation(libs.androidx.datastore.preferences)

//            implementation(libs.jetbrains.kotlinx.atomicfu)
            implementation(libs.kotlinx.collections.immutable)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
