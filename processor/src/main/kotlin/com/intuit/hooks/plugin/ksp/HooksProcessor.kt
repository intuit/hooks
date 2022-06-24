package com.intuit.hooks.plugin.ksp

import arrow.core.sequence
import arrow.core.valueOr
import arrow.typeclasses.Semigroup
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.intuit.hooks.plugin.codegen.*
import com.intuit.hooks.plugin.ksp.validation.validateProperty
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*

public class HooksProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getNewFiles().forEach {
            it.accept(HooksVisitor(), Unit)
        }

        return emptyList()
    }

    private inner class HooksVisitor : KSVisitorVoid() {

        override fun visitFile(file: KSFile, data: Unit) {
            file.declarations.filter {
                it is KSClassDeclaration
            }.forEach {
                it.accept(this, Unit)
            }
        }

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            // TODO: This should really be restructured to follow KSP visitor pattern for members
            val superTypeNames = classDeclaration.superTypes
                .map(KSTypeReference::element)
                .filterIsInstance<KSClassifierReference>()
                .map(KSClassifierReference::referencedName)

            // TODO: Account for import aliases :P + how to avoid false positives? probably through resolve
            if (!superTypeNames.contains("Hooks") && !superTypeNames.contains("HooksDsl")) {
                classDeclaration.declarations.filter {
                    it is KSClassDeclaration && it.validate()
                }.forEach {
                    it.accept(this, Unit)
                }
            } else {
                classDeclaration.findHooks()
                    .map { hooks -> createHooksContainer(classDeclaration, hooks) }
                    .map { hooksContainer ->
                        if (hooksContainer.hooks.isEmpty()) return@map
                        // TODO: somehow specify the original file as a dependency of this new file
                        hooksContainer.generateFile().writeTo(codeGenerator, aggregating = false)
                    }.valueOr { errors ->
                        errors.forEach { logger.error(it.message, it.symbol) }
                    }
            }
        }
    }

    internal fun createHooksContainer(classDeclaration: KSClassDeclaration, hooks: List<HookInfo>): HooksContainer {
        val name =
            "${classDeclaration.parentDeclaration?.simpleName?.asString() ?: ""}${classDeclaration.simpleName.asString()}Impl"
        val resolvedPackageName = classDeclaration.packageName.asString().takeIf(String::isNotEmpty)
        val visibilityModifier = classDeclaration.getVisibility().toKModifier() ?: KModifier.PUBLIC
        val typeArguments = classDeclaration.typeParameters.map { it.toTypeVariableName() }
        val className = classDeclaration.toClassName()
        val typeSpecKind = classDeclaration.classKind.toTypeSpecKind()

        return HooksContainer(
            name,
            className,
            typeSpecKind,
            resolvedPackageName,
            visibilityModifier,
            typeArguments,
            hooks
        )
    }

    private fun KSClassDeclaration.findHooks() = getAllProperties()
        .filter {
            // Only process properties that are abstract b/c that's what we need for a concrete class
            it.modifiers.contains(Modifier.ABSTRACT)
        }
        .map(::validateProperty)
        .sequence(Semigroup.nonEmptyList())

    public class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): HooksProcessor = HooksProcessor(
            environment.codeGenerator,
            environment.logger,
        )
    }

    public class Exception(message: String, cause: Throwable? = null) : kotlin.Exception(message, cause)
}

public fun ClassKind.toTypeSpecKind(): TypeSpec.Kind = when (this) {
    ClassKind.CLASS -> TypeSpec.Kind.CLASS
    ClassKind.INTERFACE -> TypeSpec.Kind.INTERFACE
    ClassKind.OBJECT -> TypeSpec.Kind.OBJECT
    else -> throw NotImplementedError("Hooks in constructs other than class, interface, and object aren't supported")
}
