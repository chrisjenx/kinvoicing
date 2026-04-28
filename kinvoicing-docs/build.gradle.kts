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
