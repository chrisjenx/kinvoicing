pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "invoice-kit"

include(":core")
include(":render-html")
include(":render-compose")
include(":render-pdf")
include(":fidelity-test")
