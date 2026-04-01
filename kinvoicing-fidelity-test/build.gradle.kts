plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    testImplementation(project(":render-html"))
    testImplementation(project(":kinvoicing-examples"))
    testImplementation(libs.compose2pdf)
    testImplementation(compose.desktop.currentOs)
    testImplementation(compose.runtime)
    testImplementation(compose.foundation)
    testImplementation(compose.ui)
    testImplementation(compose.material)
    testImplementation(libs.kotlin.test)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation(libs.pdfbox)
    testImplementation(libs.jsoup)
    testImplementation(libs.playwright)
}
