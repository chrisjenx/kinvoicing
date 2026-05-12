plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.chrisjenx"
version = "0.1.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create("wasmjsNodeCompose") {
            id = "com.chrisjenx.wasmjs-node-compose"
            implementationClass = "com.chrisjenx.wasmjsnode.WasmJsNodeComposePlugin"
            displayName = "wasmJs Node Compose Test Support"
            description = "Runs Compose Multiplatform wasmJs tests on Node.js by patching Skiko's Emscripten loader and injecting a DOM/canvas shim."
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
