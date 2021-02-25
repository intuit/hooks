// This file was automatically generated from README.md by Knit tool. Do not edit.
package com.intuit.hooks.example

import org.junit.jupiter.api.Test
import kotlinx.knit.test.*

class HooksDSLTest {
    @Test
    fun testExampleDsl01() {
        captureOutput("ExampleDsl01") { com.intuit.hooks.example.exampleDsl01.main() }.verifyOutputLines(
            "newSpeed: 30"
        )
    }
}
