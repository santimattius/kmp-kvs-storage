import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKMPLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.sqldelight)
    id("org.jetbrains.dokka")
}

group = "io.github.santimattius"
version = "2.0.0"

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-opt-in=com.santimattius.kvs.InternalKvsApi")
    }

    androidLibrary {
        namespace = "io.github.santimattius.kvs.persistence.optimized"
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
        it.binaries.framework {
            baseName = "KvsPersistenceOptimized"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.kvsCore)
            implementation(libs.kotlin.stdlib)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.androidx.startup.runtime)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}

sqldelight {
    databases {
        create("KvsDatabase") {
            packageName.set("com.santimattius.kvs.persistence.optimized.db")
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:${libs.versions.sqldelight.get()}")
        }
    }
    linkSqlite.set(true)
}

dokka {
    dokkaSourceSets.named("commonMain") {
        // KMP common symbols render once here, tagged with available platforms.
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "kvs-persistence-optimized", version.toString())
    pom {
        name = "KvsPersistenceOptimized"
        description = "KvsStorage optimized persistence backend for large datasets with efficient TTL support."
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
