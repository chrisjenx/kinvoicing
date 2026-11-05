plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

application {
    mainClass.set("com.chrisjenx.kinvoicing.docs.DocsGeneratorKt")
    applicationDefaultJvmArgs = listOf("-Dproject.root=${rootProject.projectDir.absolutePath}")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":render-html-email"))
    implementation(project(":render-pdf"))
    implementation(project(":kinvoicing-examples"))
    implementation(libs.pdfbox)
}

tasks.register<JavaExec>("renderPreviews") {
    group = "documentation"
    description = "Render payButton + linksAndImages fixtures to HTML, PDF, and PNG previews."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.chrisjenx.kinvoicing.docs.RenderPreviewsKt")
    systemProperty("project.root", rootProject.projectDir.absolutePath)
}

// CMP 1.13 publishes desktop runtime artifacts under both org.jetbrains.compose.* and androidx.compose.*
// with identical file names (e.g. runtime-saveable-desktop-1.13.0-beta02.jar), which collide in the
// application distribution's lib/ dir. This generator is only ever invoked via `run` / `renderPreviews`,
// so the distribution archives aren't needed.
tasks.named("distTar") { enabled = false }
tasks.named("distZip") { enabled = false }
