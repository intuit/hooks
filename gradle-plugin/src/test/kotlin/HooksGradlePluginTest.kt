import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import java.io.File

private fun File.appendKotlin(@Language("kotlin") content: String, trimIndent: Boolean = true) {
    require(extension.matches(Regex("kt(s)?")))
    appendText("\n${if (trimIndent) content.trimIndent() else content}\n")
}

class HooksGradlePluginTest {

    @TempDir lateinit var workingDir: File

    lateinit var buildFile: File

    @BeforeEach fun setup() {
        buildFile = workingDir.resolve("build.gradle.kts").apply(File::createNewFile)
        buildFile.appendKotlin(
            """
            repositories {
                mavenLocal()
                mavenCentral()
            }

            plugins {
                kotlin("jvm")
                id("com.intuit.hooks")
            }
        """
        )
    }

    @Test fun `can apply plugin`() {
        buildFile.appendKotlin(
            """
            hooks {}
        """
        )

        assertDoesNotThrow {
            GradleRunner.create()
                .withProjectDir(workingDir)
                .withPluginClasspath()
                .build()
        }
    }

    @Test fun `can code gen`() {
        val testHooks = workingDir.resolve("src/main/kotlin/TestHooks.kt").apply {
            parentFile.mkdirs()
            createNewFile()
        }
        testHooks.appendKotlin(
            """
            import com.intuit.hooks.SyncHook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @Sync<(String) -> Unit>
                abstract val testSyncHook: SyncHook<*>
            }
        """
        )

        val runner = GradleRunner.create()
            .withProjectDir(workingDir)
            .withArguments("build")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, runner.task(":kspKotlin")?.outcome)
        assertTrue(workingDir.resolve("build/generated/ksp/main/kotlin/TestHooksHooks.kt").exists())
    }
}
