plugins {
    `kotlin-dsl`
}

// The wasmjs-node-compose plugin reaches KGP types reflectively, so this
// buildSrc has no `kotlin-gradle-plugin` dependency. Declaring one
// produces either a plugin-resolution conflict (`implementation`) or a
// runtime NoClassDefFoundError (`compileOnly`).
