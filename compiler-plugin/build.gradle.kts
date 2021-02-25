val pluginDependency: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
}

dependencies {
    val ARROW_VERSION: String by project
    val ARROW_META_VERSION: String by project

    // these are the dependencies we're specifically including
    pluginDependency("io.arrow-kt", "arrow-core", ARROW_VERSION)
    pluginDependency("io.arrow-kt", "arrow-annotations", ARROW_VERSION)
    pluginDependency("io.arrow-kt", "arrow-syntax", ARROW_VERSION)
    pluginDependency("io.arrow-kt", "arrow-core-data", ARROW_VERSION)
    pluginDependency("io.arrow-kt", "compiler-plugin", ARROW_META_VERSION)

    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))
    compileOnly(kotlin("scripting-compiler-embeddable"))
    compileOnly(kotlin("script-util") as String) {
        exclude(kotlin("stdlib") as String)
        exclude(kotlin("compiler") as String)
        exclude(kotlin("compiler-embeddable") as String)
    }

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter", "junit-jupiter")

    testImplementation(project(":hooks"))
    testImplementation("io.arrow-kt:arrow-meta-prelude:$ARROW_META_VERSION")
    testImplementation("io.arrow-kt:arrow-core:$ARROW_VERSION")
    testImplementation("io.arrow-kt:compiler-plugin:$ARROW_META_VERSION")
    testImplementation("io.arrow-kt:meta-test:$ARROW_META_VERSION")
}

// Add new content to the default jar artifact: Arrow Meta Compiler Plugin except META-INF file to use the new one
tasks {
    jar {
        fromConfiguration(pluginDependency) {
            exclude("META-INF/services/org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar")
        }
    }
}
