plugins {
    `java-platform`
    alias(libs.plugins.mavenPublish)
    id("org.jetbrains.dokka")
}

group = "io.github.santimattius"
version = "2.0.0"

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(projects.kvsCore)
        api(projects.kvsPersistenceLight)
        api(projects.kvsPersistenceOptimized)
        api(projects.kvsDocument)
        api(projects.shared)
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "kvs-bom", version.toString())
    pom {
        name = "KvsBom"
        description = "Bill of Materials for KvsStorage — aligns versions of all published artifacts."
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
