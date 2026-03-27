plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
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

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

android {
    namespace = "com.chrisjenx.kinvoicing.compose"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}
