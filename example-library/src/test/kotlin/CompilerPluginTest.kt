package com.intuit.hooks.example.library

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

class CompilerPluginTest {

    @Test
    fun `sources are generated in specified directory`() {
        val generatedDirPath = Paths.get(System.getProperty("user.dir"), "build", "generated")
        listOf("CarHooks", "GenericHooks").map {
            Paths.get(
                generatedDirPath.toString(),
                "source",
                "kapt",
                "main",
                "${it}Impl.kt"
            )
        }
            .map(Path::exists)
            .forEach(Assertions::assertTrue)
    }
}
