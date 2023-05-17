package com.intuit.hooks.plugin.ksp

import arrow.core.*
import arrow.core.raise.Raise
import arrow.core.raise.recover
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.intuit.hooks.plugin.codegen.*
import com.intuit.hooks.plugin.ksp.validation.*
import com.intuit.hooks.plugin.ksp.validation.EdgeCase
import com.intuit.hooks.plugin.ksp.validation.HookValidationError
import com.intuit.hooks.plugin.ksp.validation.error
import com.intuit.hooks.plugin.ksp.validation.validateProperty
import com.intuit.hooks.plugin.mapOrAccumulate
import com.intuit.hooks.plugin.raise
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*

public class HooksProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getNewFiles().forEach {
            it.accept(HookFileVisitor(), Unit)
        }

        return emptyList()
    }

    private inner class HookPropertyVisitor : KSRaiseVisitor<TypeParameterResolver, HookInfo, HookValidationError>() {
        context(Raise<Nel<HookValidationError>>)
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: TypeParameterResolver): HookInfo =
            property.validateProperty(data)
    }

    private inner class HookFileVisitor : KSVisitorVoid() {
        override fun visitFile(file: KSFile, data: Unit) {
            recover({
                val containers = file.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .flatMap { it.accept(HookContainerVisitor(), Unit) }
                    .ifEmpty { raise(EdgeCase.NoHooksDefined(file)) }

                val packageName = file.packageName.asString()
                val name = file.fileName.split(".").first()

                // May raise some additional errors
                generateFile(packageName, "${name}Hooks", containers.toList())
                    .writeTo(codeGenerator, aggregating = false, originatingKSFiles = listOf(file))
            }, { errors: Nel<LogicalFailure> ->
                errors.filterIsInstance<HookValidationError>().forEach(logger::error)
            }, { throwable: Throwable ->
                logger.error("Uncaught exception while processing file: ${throwable.localizedMessage}", file)
                logger.exception(throwable)
            },)
        }
    }

    private inner class HookContainerVisitor : KSRaiseVisitor<Unit, Sequence<HooksContainer>, HookValidationError>() {

        context(Raise<Nel<HookValidationError>>)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Sequence<HooksContainer> {
            val superTypeNames = classDeclaration.superTypes
                .filter { it.toString().contains("Hooks") }
                .toList()

            return if (superTypeNames.isEmpty()) {
                classDeclaration.declarations
                    .filter { it is KSClassDeclaration && it.validate() /* TODO: Tie in validations to KSP */ }
                    .flatMap { it.accept(this@HookContainerVisitor, Unit) }
            } else if (superTypeNames.any { it.resolve().declaration.qualifiedName?.getQualifier() == "com.intuit.hooks.dsl" }) {
                val parentResolver = classDeclaration.typeParameters.toTypeParameterResolver()

                classDeclaration.getAllProperties()
                    .mapOrAccumulate { it.accept(HookPropertyVisitor(), parentResolver) }
                    .let { createHooksContainer(classDeclaration, it) }
                    .let { sequenceOf(it) }
            } else {
                emptySequence()
            }
        }

        context(Raise<Nel<HookValidationError>>)
        fun KSClassDeclaration.toTypeSpecKind(): TypeSpec.Kind = when (classKind) {
            ClassKind.CLASS -> TypeSpec.Kind.CLASS
            ClassKind.INTERFACE -> TypeSpec.Kind.INTERFACE
            ClassKind.OBJECT -> TypeSpec.Kind.OBJECT
            else -> raise(HookValidationError.UnsupportedContainer(this))
        }

        context(Raise<Nel<HookValidationError>>)
        fun createHooksContainer(classDeclaration: KSClassDeclaration, hooks: List<HookInfo>): HooksContainer {
            val name =
                "${classDeclaration.parentDeclaration?.simpleName?.asString() ?: ""}${classDeclaration.simpleName.asString()}Impl"
            val visibilityModifier = classDeclaration.getVisibility().toKModifier() ?: KModifier.PUBLIC
            val typeArguments = classDeclaration.typeParameters.map { it.toTypeVariableName() }
            val className = classDeclaration.toClassName()
            val typeSpecKind = classDeclaration.toTypeSpecKind()

            return HooksContainer(
                name,
                className,
                typeSpecKind,
                visibilityModifier,
                typeArguments,
                hooks,
            )
        }
    }

    public class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): HooksProcessor = HooksProcessor(
            environment.codeGenerator,
            environment.logger,
        )
    }

    public class Exception(message: String, cause: Throwable? = null) : kotlin.Exception(message, cause)
}
