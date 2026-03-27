plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    explicitApi()
    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(project(":core"))
                implementation(project(":render-compose"))
                implementation(libs.compose2pdf)
                implementation(compose.desktop.currentOs)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.pdfbox)
            }
        }
    }
}
