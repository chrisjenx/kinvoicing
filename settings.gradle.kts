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
include(":render-html-email")
include(":render-compose")
include(":render-pdf")
include(":fidelity-test")
include(":render-html")
include(":kinvoicing-fidelity-test")
include(":kinvoicing-examples")
include(":kinvoicing-docs")
