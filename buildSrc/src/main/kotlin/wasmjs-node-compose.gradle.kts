// Makes Compose Multiplatform's wasmJs tests runnable under `nodejs()`
// instead of `browser()` (Karma+Chrome). Apply to a KMP module that
// declares `wasmJs { nodejs() }` and depends on Compose / Skiko.
//
// Caveats:
//   - The skiko.mjs gate substitution and `awaitSkiko` promise export
//     are properties of the current Skiko build; a Compose / Skiko bump
//     may invalidate either.
//   - Patched skiko.mjs is incompatible with simultaneous `browser()`:
//     Webpack can't bundle the Node-only `await import("module")`.

private val nodeTestShimContent = """
    const windowStub = {
      addEventListener: () => {}, removeEventListener: () => {},
      postMessage: () => {},
      setTimeout: (fn, ms) => setTimeout(fn, ms),
      clearTimeout: (id) => clearTimeout(id),
      setInterval: (fn, ms) => setInterval(fn, ms),
      clearInterval: (id) => clearInterval(id),
      requestAnimationFrame: (fn) => setTimeout(() => fn(Date.now()), 16),
      cancelAnimationFrame: (id) => clearTimeout(id),
      location: { href: 'http://localhost/', origin: 'http://localhost' },
    };
    globalThis.window = windowStub;
    globalThis.self = windowStub;
    globalThis.document = {
      createElement: () => ({ getContext: () => null, style: {}, addEventListener: () => {} }),
      documentElement: { style: {} },
      body: { appendChild: () => {}, style: {} },
      addEventListener: () => {}, removeEventListener: () => {},
    };
    if (!globalThis.navigator) {
      globalThis.navigator = { userAgent: 'node', language: 'en-US', languages: ['en-US'] };
    }
    const skiko = await import('./kotlin/skiko.mjs');
    await skiko.awaitSkiko;
""".trimIndent()

tasks.matching { it.name == "wasmJsNodeTest" }.configureEach {
    // Resolve paths inside configureEach so the doFirst lambda below
    // closes over plain File / String — never an implicit `this` to the
    // script class, which the configuration cache refuses to serialize.
    val pkgDir = rootProject.layout.buildDirectory
        .dir("wasm/packages/${rootProject.name}-${project.name}-test")
        .get().asFile
    val skikoMjs = pkgDir.resolve("kotlin/skiko.mjs")
    val nodeTestShim = pkgDir.resolve("node-test-shim.mjs")
    val shimText = nodeTestShimContent

    // The reflective Method lookup below still defeats CC serialization;
    // opt this single task out, leave the rest of the build on CC.
    notCompatibleWithConfigurationCache(
        "wasmjs-node-compose mutates nodeJsArgs reflectively"
    )

    doFirst {
        if (skikoMjs.exists()) {
            val text = skikoMjs.readText()
            if (text.contains("if (false) {")) {
                skikoMjs.writeText(
                    text.replace("if (false) {", "if (ENVIRONMENT_IS_NODE) {")
                )
            }
        }
        nodeTestShim.parentFile.mkdirs()
        nodeTestShim.writeText(shimText + "\n")

        // Reach KotlinJsTest.nodeJsArgs reflectively so buildSrc doesn't
        // need kotlin-gradle-plugin on its classpath.
        @Suppress("UNCHECKED_CAST")
        val nodeJsArgs = runCatching {
            javaClass.getMethod("getNodeJsArgs").invoke(this) as MutableList<String>
        }.getOrNull()
        if (nodeJsArgs != null) {
            val shimUrl = nodeTestShim.toURI().toASCIIString()
            if (!nodeJsArgs.contains(shimUrl)) {
                nodeJsArgs.add("--import")
                nodeJsArgs.add(shimUrl)
            }
        }
    }
}
