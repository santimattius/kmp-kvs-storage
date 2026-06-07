import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKMPLibrary)
    alias(libs.plugins.skie)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.santimattius"
version = "2.0.0"

kotlin {
    androidLibrary {
        namespace = "io.github.santimattius.kvs"

        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    val xcFrameworkName = "KvsStorage"
    val xcf = XCFramework(xcFrameworkName)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = xcFrameworkName
            binaryOption("bundleId", "io.github.santimattius.kvs.${xcFrameworkName}")
            xcf.add(this)
            isStatic = true
            export(projects.kvsCore)
            export(projects.kvsPersistenceLight)
            export(projects.kvsDocument)
            export(projects.kvsPersistenceOptimized)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.kvsCore)
            api(projects.kvsPersistenceLight)
            api(projects.kvsDocument)
            api(projects.kvsPersistenceOptimized)
        }
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
    coordinates(group.toString(), "kvs", version.toString())
    pom {
        name = "KvsStorage"
        description = "Convenience aggregator for all KvsStorage 2.0 modules. Prefer individual artifacts for smaller binaries."
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
