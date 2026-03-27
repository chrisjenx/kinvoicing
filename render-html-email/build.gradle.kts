plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

kotlin {
    explicitApi()

    jvm()
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    wasmJs { browser() }
    macosArm64()
    macosX64()
    linuxX64()
    linuxArm64()
    mingwX64()

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

android {
    namespace = "com.chrisjenx.kinvoicing.html.email"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}
