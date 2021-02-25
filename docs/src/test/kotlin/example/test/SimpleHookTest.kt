// This file was automatically generated from README.md by Knit tool. Do not edit.
package com.intuit.hooks.example

import org.junit.jupiter.api.Test
import kotlinx.knit.test.*

class SimpleHookTest {
    @Test
    fun testExampleSynchook01() {
        captureOutput("ExampleSynchook01") { com.intuit.hooks.example.exampleSynchook01.main() }.verifyOutputLines(
            "my hook was called"
        )
    }
}
