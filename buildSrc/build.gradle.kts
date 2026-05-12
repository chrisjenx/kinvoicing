plugins {
    `kotlin-dsl`
}

// No KGP dependency on buildSrc's classpath: the wasmjs-node-compose plugin
// reaches `nodeJsArgs` reflectively on the `wasmJsNodeTest` task, avoiding
// version-conflict and "two classloaders" pitfalls that come from
// declaring `kotlin-gradle-plugin` here.
