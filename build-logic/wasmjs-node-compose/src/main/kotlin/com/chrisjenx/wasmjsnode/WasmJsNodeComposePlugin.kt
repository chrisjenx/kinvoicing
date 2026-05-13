package com.chrisjenx.wasmjsnode

import org.gradle.api.Plugin
import org.gradle.api.Project

class WasmJsNodeComposePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val shimText = loadShimResource()
        target.tasks.matching { it.name == "wasmJsNodeTest" }.configureEach {
            val pkgDir = target.rootProject.layout.buildDirectory
                .dir("wasm/packages/${target.rootProject.name}-${target.name}-test")
                .get().asFile
            val skikoMjs = pkgDir.resolve("kotlin/skiko.mjs")
            val nodeTestShim = pkgDir.resolve("node-test-shim.mjs")

            notCompatibleWithConfigurationCache(
                "wasmjs-node-compose mutates nodeJsArgs reflectively"
            )

            doFirst {
                if (skikoMjs.exists()) {
                    val text = skikoMjs.readText()
                    // Undo Emscripten DCE: skiko.mjs ships with `if (false) {`
                    // around its Node loader; flip it so module resolution works
                    // under nodejs(). Skiko version bumps may invalidate this gate.
                    if (text.contains("if (false) {")) {
                        skikoMjs.writeText(
                            text.replace("if (false) {", "if (ENVIRONMENT_IS_NODE) {")
                        )
                    }
                }
                nodeTestShim.parentFile.mkdirs()
                nodeTestShim.writeText(shimText + "\n")

                @Suppress("UNCHECKED_CAST")
                val nodeJsArgs = try {
                    javaClass.getMethod("getNodeJsArgs").invoke(this) as MutableList<String>
                } catch (_: NoSuchMethodException) {
                    null
                }
                if (nodeJsArgs != null) {
                    val shimUrl = nodeTestShim.toURI().toASCIIString()
                    if (!nodeJsArgs.contains(shimUrl)) {
                        nodeJsArgs.add("--import")
                        nodeJsArgs.add(shimUrl)
                    }
                }
            }
        }
    }

    private fun loadShimResource(): String {
        val url = javaClass.getResource("/com/chrisjenx/wasmjsnode/node-test-shim.mjs")
            ?: error("node-test-shim.mjs resource missing from plugin JAR")
        return url.readText()
    }
}
