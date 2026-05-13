plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
    id("com.chrisjenx.wasmjs-node-compose")
}

kotlin {
    explicitApi()

    jvm()
    androidTarget()
    iosArm64()
    iosSimulatorArm64 {
        binaries.withType(org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable::class.java).configureEach {
            // CMP 1.11+ ships compose.ui-uikit symbols referencing iOS 18
            // APIs (UIViewLayoutRegion). Static test-executable linking
            // pulls them in even when consumer apps don't. Bump the test
            // binary's deployment target — main framework unaffected.
            linkerOpts("-platform_version", "ios-simulator", "18.0", "18.0")
        }
    }
    wasmJs { nodejs() }

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
