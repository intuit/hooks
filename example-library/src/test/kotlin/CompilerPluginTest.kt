package com.intuit.hooks.example.library

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.io.path.exists

class CompilerPluginTest {

    @Test
    fun `sources are generated in specified directory`() {
        val generatedDirPath = Paths.get(System.getProperty("user.dir"), "build", "generated")
        listOf("car.CarHooks", "generic.GenericHooks").map {
            "com.intuit.hooks.example.library.$it"
        }.map {
            Paths.get("", *it.split(".").toTypedArray())
        }.map {
            Paths.get(
                generatedDirPath.toString(),
                "source",
                "kapt",
                "main",
            ).resolve("${it}Impl.kt")
        }.forEach {
            assertTrue(it.exists()) { "$it does not exist" }
        }
    }
}
