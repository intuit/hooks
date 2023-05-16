package com.intuit.hooks.plugin.ksp

import arrow.core.*
import arrow.core.raise.Raise
import arrow.core.raise.recover
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.intuit.hooks.plugin.codegen.*
import com.intuit.hooks.plugin.ksp.validation.*
import com.intuit.hooks.plugin.ksp.validation.EdgeCase
import com.intuit.hooks.plugin.ksp.validation.HookValidationError
import com.intuit.hooks.plugin.ksp.validation.error
import com.intuit.hooks.plugin.ksp.validation.validateProperty
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*

public class HooksProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getNewFiles().forEach {
            it.accept(HookFileVisitor(), Unit)
        }

        return emptyList()
    }

    private inner class HookPropertyVisitor : KSDefaultVisitor<TypeParameterResolver, HookInfo>() {

        context(Raise<Nel<HookValidationError>>)
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, parentResolver: TypeParameterResolver): HookInfo {
            ensure(property.modifiers.contains(Modifier.ABSTRACT)) {
                HookValidationError.NotAnAbstractProperty(property)
            }

            return property.validateProperty(parentResolver)
        }

        override fun defaultHandler(node: KSNode, data: TypeParameterResolver) = error("Should not happen.")
    }

    private inner class HookFileVisitor : KSVisitorVoid() {
        override fun visitFile(file: KSFile, data: Unit) {
            recover({
                val containers = file.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .flatMap { it.accept(HookContainerVisitor(), this) }
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
            })
        }
    }

    private inner class HookContainerVisitor : KSDefaultVisitor<Raise<Nel<HookValidationError>>, Sequence<HooksContainer>>() {
        // TODO: Try with context receiver
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            raise: Raise<Nel<HookValidationError>>
        ): Sequence<HooksContainer> = with(raise) {
            val superTypeNames = classDeclaration.superTypes
                .filter { it.toString().contains("Hooks") }
                .toList()

            return if (superTypeNames.isEmpty()) {
                classDeclaration.declarations
                    .filter { it is KSClassDeclaration && it.validate() /* TODO: Tie in validations to KSP */ }
                    .flatMap { it.accept(this@HookContainerVisitor, raise) }
            } else if (superTypeNames.any { it.resolve().declaration.qualifiedName?.getQualifier() == "com.intuit.hooks.dsl" }) {
                val parentResolver = classDeclaration.typeParameters.toTypeParameterResolver()

                classDeclaration.getAllProperties()
                    .map { it.accept(HookPropertyVisitor(), parentResolver) }
                    // TODO: Maybe curry class declaration
                    .run { createHooksContainer(classDeclaration, toList()) }
                    .let { sequenceOf(it) }
            } else emptySequence()
        }

        fun ClassKind.toTypeSpecKind(): TypeSpec.Kind = when (this) {
            ClassKind.CLASS -> TypeSpec.Kind.CLASS
            ClassKind.INTERFACE -> TypeSpec.Kind.INTERFACE
            ClassKind.OBJECT -> TypeSpec.Kind.OBJECT
            else -> throw NotImplementedError("Hooks in constructs other than class, interface, and object aren't supported")
        }

        fun createHooksContainer(classDeclaration: KSClassDeclaration, hooks: List<HookInfo>): HooksContainer {
            val name =
                "${classDeclaration.parentDeclaration?.simpleName?.asString() ?: ""}${classDeclaration.simpleName.asString()}Impl"
            val visibilityModifier = classDeclaration.getVisibility().toKModifier() ?: KModifier.PUBLIC
            val typeArguments = classDeclaration.typeParameters.map { it.toTypeVariableName() }
            val className = classDeclaration.toClassName()
            val typeSpecKind = classDeclaration.classKind.toTypeSpecKind()

            return HooksContainer(
                name,
                className,
                typeSpecKind,
                visibilityModifier,
                typeArguments,
                hooks
            )
        }

        override fun defaultHandler(node: KSNode, data: Raise<Nel<HookValidationError>>) = TODO("Not yet implemented")
    }

    public class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): HooksProcessor = HooksProcessor(
            environment.codeGenerator,
            environment.logger,
        )
    }

    public class Exception(message: String, cause: Throwable? = null) : kotlin.Exception(message, cause)
}
