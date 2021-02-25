// This file was automatically generated from key-concepts.md by Knit tool. Do not edit.
package com.intuit.hooks.example

import org.junit.jupiter.api.Test
import kotlinx.knit.test.*

class HookContextTest {
    @Test
    fun testExampleContext01() {
        captureOutput("ExampleContext01") { com.intuit.hooks.example.exampleContext01.main() }.verifyOutputLines(
            "NoisePlugin is doing it's job",
            "Silence..."
        )
    }
}
