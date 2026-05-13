package com.chrisjenx.wasmjsnode

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class WasmJsNodeComposePluginTest {

    @TempDir lateinit var projectDir: File

    @Test
    fun `plugin applies cleanly to an empty project`() {
        projectDir.resolve("settings.gradle.kts").writeText(
            """rootProject.name = "smoke""""
        )
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins { id("com.chrisjenx.wasmjs-node-compose") }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("help", "--no-configuration-cache")
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), "expected BUILD SUCCESSFUL in:\n${result.output}")
    }

    @Test
    fun `plugin writes shim and patches nothing when wasmJsNodeTest runs without real wasm output`() {
        projectDir.resolve("settings.gradle.kts").writeText(
            """rootProject.name = "smoke""""
        )
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins { id("com.chrisjenx.wasmjs-node-compose") }
            tasks.register("wasmJsNodeTest") {
                doLast { println("=== ran wasmJsNodeTest ===") }
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("wasmJsNodeTest", "--no-configuration-cache")
            .build()

        assertTrue(
            result.output.contains("=== ran wasmJsNodeTest ==="),
            "expected wasmJsNodeTest to run; output:\n${result.output}"
        )

        val shimFile = projectDir.resolve("build/wasm/packages/smoke-smoke-test/node-test-shim.mjs")
        assertTrue(shimFile.exists(), "Expected shim to be written at $shimFile")
        val shimText = shimFile.readText()
        assertTrue(shimText.contains("windowStub"), "shim missing windowStub: $shimText")
        assertTrue(shimText.contains("await skiko.awaitSkiko"), "shim missing awaitSkiko: $shimText")
    }
}
