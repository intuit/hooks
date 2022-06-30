package com.intuit.hooks.plugin.ksp

import arrow.core.*
import arrow.typeclasses.Semigroup
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.intuit.hooks.plugin.codegen.*
import com.intuit.hooks.plugin.ksp.validation.HookValidationError
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

    private inner class HookPropertyVisitor : KSDefaultVisitor<TypeParameterResolver, ValidatedNel<HookValidationError, HookInfo>>() {
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, parentResolver: TypeParameterResolver): ValidatedNel<HookValidationError, HookInfo> {
            return if (property.modifiers.contains(Modifier.ABSTRACT))
                validateProperty(property, parentResolver)
            else
                HookValidationError.NotAnAbstractProperty(property).invalidNel()
        }

        override fun defaultHandler(node: KSNode, data: TypeParameterResolver): ValidatedNel<HookValidationError, HookInfo> =
            TODO("Not yet implemented")
    }

    private inner class HookFileVisitor : KSVisitorVoid() {
        override fun visitFile(file: KSFile, data: Unit) {
            val hookContainers = file.declarations.filter {
                it is KSClassDeclaration
            }.flatMap {
                it.accept(HookContainerVisitor(), Unit)
            }.mapNotNull { v ->
                v.valueOr { errors ->
                    errors.forEach { error -> logger.error(error.message, error.symbol) }
                    null
                }
            }.toList()

            if(hookContainers.isEmpty()) return

            val packageName = file.packageName.asString()
            val name = file.fileName.split(".").first()

            generateFile(packageName, "${name}Hooks", hookContainers).writeTo(codeGenerator, aggregating = false, originatingKSFiles = listOf(file))
        }
    }

    private inner class HookContainerVisitor : KSDefaultVisitor<Unit, List<ValidatedNel<HookValidationError, HooksContainer>>>() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): List<ValidatedNel<HookValidationError, HooksContainer>> {
            val superTypeNames = classDeclaration.superTypes
                .filter { it.toString().contains("Hooks") }
                .toList()

            return if (superTypeNames.isEmpty()) {
                classDeclaration.declarations
                    .filter { it is KSClassDeclaration && it.validate() }
                    .flatMap { it.accept(this, Unit) }
                    .toList()
            } else if (superTypeNames.any { it.resolve().declaration.qualifiedName?.getQualifier() == "com.intuit.hooks.dsl" }) {
                val parentResolver = classDeclaration.typeParameters.toTypeParameterResolver()

                classDeclaration.getAllProperties()
                    .map { it.accept(HookPropertyVisitor(), parentResolver) }
                    .sequence(Semigroup.nonEmptyList())
                    .map { hooks -> createHooksContainer(classDeclaration, hooks) }
                    .let(::listOf)
            } else {
                emptyList()
            }
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

        override fun defaultHandler(node: KSNode, data: Unit): List<ValidatedNel<HookValidationError, HooksContainer>> =
            TODO("Not yet implemented")
    }


    public class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): HooksProcessor = HooksProcessor(
            environment.codeGenerator,
            environment.logger,
        )
    }

    public class Exception(message: String, cause: Throwable? = null) : kotlin.Exception(message, cause)
}
