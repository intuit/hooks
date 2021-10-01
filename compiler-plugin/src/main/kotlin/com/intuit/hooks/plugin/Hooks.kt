package com.intuit.hooks.plugin

import arrow.core.*
import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
import arrow.meta.quotes.classorobject.ClassDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtImportList

internal val Meta.hooks: CliPlugin
    get() =
        "Hooks" {
            meta(
                classDeclaration(this, { this.element.isHooksDslClass }) { c ->
                    findHooks().map<Transform<KtClass>> { codeGen ->
                        val `package` = c.element.containingKtFile.packageDirective?.text ?: ""
                        val (classes, properties) = codeGen.map(::generateHookClass).unzip()
                        val imports = createImportDirectives(c.element, codeGen)

                        val newSource =
                            """|${`package`}
                           |
                           |$imports
                           |
                           |$kind ${name}Impl${this.`(typeParameters)`} : $name${this.`(typeParameters)`}() {
                           |   ${properties.map { it.property(null) }.joinToString("\n")}
                           |   ${classes.map { it.`class` }.joinToString("\n")} 
                           |}""".trimMargin().file("${name}Impl")

                        Transform.newSources(newSource)
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
