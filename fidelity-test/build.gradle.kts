plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    sourceSets {
        jvmTest {
            dependencies {
                implementation(project(":core"))
                implementation(project(":render-compose"))
                implementation(project(":render-html"))
                implementation(project(":render-pdf"))
                implementation(libs.kotlin.test)
                implementation(libs.pdfbox)
                implementation(libs.playwright)
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation("org.junit.jupiter:junit-jupiter:5.11.3")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "2g"
}
