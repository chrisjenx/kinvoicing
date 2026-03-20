plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    testImplementation(project(":compose-pdf-core"))
    testImplementation(compose.desktop.currentOs)
    testImplementation(compose.runtime)
    testImplementation(compose.foundation)
    testImplementation(compose.ui)
    testImplementation(compose.material)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.pdfbox)
}
