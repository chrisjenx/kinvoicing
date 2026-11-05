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
            // CMP 1.11+'s compose.ui-uikit references iOS 18 symbols; test linkage needs the bump (main framework unaffected).
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
                implementation(libs.compose.material3)
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
    compileSdk {
        version = release(37) { minorApiLevel = 1 }
    }
    defaultConfig {
        minSdk = 23
    }
    // AGP 9.5 registers a Compose Preview runfiles task per variant (whenever compose is enabled) that
    // refuses to configure unless that variant has unit tests that include compiled Android resources.
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

// AGP 9 disables release unit tests by default (AGP 8 had them on); re-enable them so the per-variant
// Compose Preview runfiles task above can configure for 'release' too.
androidComponents {
    beforeVariants { variant ->
        variant.hostTests[com.android.build.api.variant.HostTestBuilder.UNIT_TEST_TYPE]?.enable = true
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}
