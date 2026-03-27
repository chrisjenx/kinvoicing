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

rootProject.name = "kinvoicing"

include(":core")
include(":render-html")
include(":render-compose")
include(":render-pdf")
include(":fidelity-test")
include(":kinvoicing-html")
include(":kinvoicing-fidelity-test")
include(":kinvoicing-examples")
