pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "compose-pdf"

include(":compose-pdf-core")
include(":compose-pdf-test")
include(":sample-desktop")
include(":sample-ktor")
