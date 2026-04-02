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
    implementation(project(":kinvoicing-examples"))
}
