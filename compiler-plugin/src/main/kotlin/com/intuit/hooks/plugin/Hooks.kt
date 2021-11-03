package com.intuit.hooks.plugin

import arrow.core.*
import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.phases.analysis.DefaultElementScope.Companion.DEFAULT_GENERATED_SRC_PATH
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
import arrow.meta.quotes.classorobject.ClassDeclaration
import com.pinterest.ktlint.core.KtLint.Params
import com.pinterest.ktlint.core.KtLint.format
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import java.nio.file.Paths

internal val Meta.hooks: CliPlugin
    get() =
        "Hooks" {
            meta(
                classDeclaration(this, { this.element.isHooksDslClass }) { c ->
                    findHooks().map<Transform<KtClass>> { codeGen ->
                        val `class` = c.element
                        val file = `class`.containingKtFile
                        val `package` = file.packageDirective?.text ?: ""
                        val (classes, properties) = codeGen.map(::generateHookClass).unzip()
                        val imports = createImportDirectives(`class`, codeGen)
                        val filePath = DEFAULT_GENERATED_SRC_PATH.resolve(
                            Paths.get(
                                "",
                                *file.packageFqName.asString().split(".").toTypedArray()
                            )
                        )
                        val name = "${if (`class`.isTopLevel()) "" else
                            `class`.containingClassOrObject?.name ?: ""}${name}Impl"

                        val newSource =
                            """|${`package`}
                           |
                           |$imports
                           |
                           |${visibility ?: ""} $kind $name$`(typeParameters)` : ${value.fqName}$`(typeParameters)`() {
                           |   ${properties.map { it.property(null) }.joinToString("\n")}
                           |   ${classes.map { it.`class` }.joinToString("\n")} 
                           |}""".trimMargin()

                        val formatted = Params(
                            text = newSource,
                            ruleSets = listOf(StandardRuleSetProvider().get()),
                            cb = { _, _ -> }
                        )
                            .let(::format)
                            .file(name, filePath.toString())

                        Transform.newSources(formatted)
                    }.valueOr {
                        reportHookErrors(it)
                        Transform.empty
                    }
                }
            )
        }

private fun generateHookClass(hookCodeGen: HookCodeGen): Pair<String, String> {
    val classDefinition = hookCodeGen.generateClass()
    val propertyDefinition = hookCodeGen.generateProperty()

    return classDefinition to propertyDefinition
}

private fun createImportDirectives(c: KtClass, codeGens: List<HookCodeGen>): String {
    val existingImports = c.containingKtFile.importList
        ?.removeHooksDslImport()
        ?.map { it.text ?: "" }
        ?: emptyList()

    val newImports = codeGens.flatMap { it.generateImports() }

    val hookStarImport = listOf("import com.intuit.hooks.*")

    return (hookStarImport + existingImports + newImports)
        .distinct()
        .joinToString("\n")
}

private fun KtImportList.removeHooksDslImport() =
    imports.filter { !it.text.contains("com.intuit.hooks.dsl.") }

private fun ClassDeclaration.findHooks() =
    body.properties.value
        .map(::validateHook)
        .sequenceValidated()
