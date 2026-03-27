plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    explicitApi()
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))
                implementation(libs.kotlinx.html)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
