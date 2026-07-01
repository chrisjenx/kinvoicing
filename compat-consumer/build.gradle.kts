plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    application
}

val composeVersion = providers.gradleProperty("composeVersion").orElse("1.11.1").get()
val kinvoicingVersion = providers.gradleProperty("kinvoicingVersion").orNull
    ?: error("compat-consumer requires -PkinvoicingVersion=<published version> (e.g. 0.0.0-compat-SNAPSHOT, matching the root VERSION_NAME override used to publishToMavenLocal — the -SNAPSHOT suffix makes signing not-required)")
val skikoVersion = providers.gradleProperty("skikoVersion").orNull // fallback override; normally unset

kotlin {
    // Match the JVM bytecode target of the published kinvoicing jars (built on JDK 21).
    jvmToolchain(17)
}

dependencies {
    // The PUBLISHED kinvoicing renderers. render-pdf is a KMP-jvm module; depending on the
    // umbrella coordinate lets Gradle metadata resolve the -jvm variant. core arrives
    // transitively (render-pdf exposes it via api(project(":core"))).
    implementation("com.chrisjenx.kinvoicing:render-html:$kinvoicingVersion")
    implementation("com.chrisjenx.kinvoicing:render-pdf:$kinvoicingVersion")
    implementation(compose.desktop.currentOs)
}

application {
    mainClass.set("com.chrisjenx.compat.SmokeKt")
}

// The kinvoicing renderers are published against their pinned base Compose version and request
// it transitively; without this, conflict resolution would UPGRADE the target back to the base
// and the smoke check would run on the wrong runtime. Forcing the CMP-umbrella Compose groups to
// the target makes each module resolve its own POM, so Skiko cascades to the version that Compose
// version declares — no per-row Skiko bookkeeping. If some version ever fails to cascade Skiko,
// pass -PskikoVersion=<v> to pin it explicitly.
//
// We force by ALLOWLIST, not by the "org.jetbrains.compose" prefix: several Compose subgroups are
// versioned INDEPENDENTLY of the umbrella — material3 (e.g. 1.9.0 inside CMP 1.11.1) and its
// internal deps (annotation-internal, collection-internal). Prefix-forcing them to the umbrella
// version asks for artifacts that don't exist. Those groups aren't part of the CMP runtime reshape
// that render-html / render-pdf exercise, so we leave them on the version the published POM requests.
val umbrellaComposeGroups = setOf(
    "org.jetbrains.compose.runtime",
    "org.jetbrains.compose.foundation",
    "org.jetbrains.compose.ui",
    "org.jetbrains.compose.material",
    "org.jetbrains.compose.animation",
    "org.jetbrains.compose.components",
    "org.jetbrains.compose.desktop",
)
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group in umbrellaComposeGroups) {
            useVersion(composeVersion)
            because("compat runtime swap: exercise the published binary on Compose $composeVersion")
        }
        if (skikoVersion != null && requested.group == "org.jetbrains.skiko") {
            useVersion(skikoVersion)
        }
    }
}
