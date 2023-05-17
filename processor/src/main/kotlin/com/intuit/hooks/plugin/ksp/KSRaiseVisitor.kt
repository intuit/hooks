package com.intuit.hooks.plugin.ksp

import arrow.core.Nel
import arrow.core.raise.Raise
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.intuit.hooks.plugin.ksp.RaiseContext.Companion.RaiseContext

internal data class RaiseContext<D, E>(
    val raise: Raise<Nel<E>>,
    val data: D
) {
    companion object {
        fun <D, E> Raise<Nel<E>>.RaiseContext(data: D): RaiseContext<D, E> = RaiseContext(this, data)
    }
}

/** Visitor extension to execute all visitations within the context of a [Raise] */
internal abstract class KSRaiseVisitor<D, R, E> : KSDefaultVisitor<RaiseContext<D, E>, R>() {

    context(Raise<Nel<E>>)
    open fun defaultHandler(node: KSNode, data: D): R = error("KSRaiseVisitor default implementation. This shouldn't happen unless the visitor doesn't provide the right overrides.")

    final override fun defaultHandler(node: KSNode, data: RaiseContext<D, E>): R = with(data.raise) {
        defaultHandler(node, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitNode(node: KSNode, data: D): R {
        return defaultHandler(node, data)
    }

    final override fun visitNode(node: KSNode, data: RaiseContext<D, E>): R = with(data.raise) {
        visitNode(node, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitAnnotated(annotated: KSAnnotated, data: D): R {
        return defaultHandler(annotated, data)
    }

    final override fun visitAnnotated(annotated: KSAnnotated, data: RaiseContext<D, E>): R = with(data.raise) {
        visitAnnotated(annotated, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitAnnotation(annotation: KSAnnotation, data: D): R {
        return defaultHandler(annotation, data)
    }

    final override fun visitAnnotation(annotation: KSAnnotation, data: RaiseContext<D, E>): R = with(data.raise) {
        visitAnnotation(annotation, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: D): R {
        return defaultHandler(modifierListOwner, data)
    }

    final override fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: RaiseContext<D, E>): R = with(data.raise) {
        visitModifierListOwner(modifierListOwner, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitDeclaration(declaration: KSDeclaration, data: D): R {
        return defaultHandler(declaration, data)
    }

    final override fun visitDeclaration(declaration: KSDeclaration, data: RaiseContext<D, E>): R = with(data.raise) {
        visitDeclaration(declaration, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: D): R {
        return defaultHandler(declarationContainer, data)
    }

    final override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: RaiseContext<D, E>): R = with(data.raise) {
        visitDeclarationContainer(declarationContainer, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitDynamicReference(reference: KSDynamicReference, data: D): R {
        return defaultHandler(reference, data)
    }

    final override fun visitDynamicReference(reference: KSDynamicReference, data: RaiseContext<D, E>): R = with(data.raise) {
        visitDynamicReference(reference, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitFile(file: KSFile, data: D): R {
        return defaultHandler(file, data)
    }

    final override fun visitFile(file: KSFile, data: RaiseContext<D, E>): R = with(data.raise) {
        visitFile(file, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: D): R {
        return defaultHandler(function, data)
    }

    final override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: RaiseContext<D, E>): R = with(data.raise) {
        visitFunctionDeclaration(function, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitCallableReference(reference: KSCallableReference, data: D): R {
        return defaultHandler(reference, data)
    }

    final override fun visitCallableReference(reference: KSCallableReference, data: RaiseContext<D, E>): R = with(data.raise) {
        visitCallableReference(reference, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitParenthesizedReference(reference: KSParenthesizedReference, data: D): R {
        return defaultHandler(reference, data)
    }

    final override fun visitParenthesizedReference(reference: KSParenthesizedReference, data: RaiseContext<D, E>): R = with(data.raise) {
        visitParenthesizedReference(reference, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: D): R {
        return defaultHandler(property, data)
    }

    final override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: RaiseContext<D, E>): R = with(data.raise) {
        visitPropertyDeclaration(property, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: D): R {
        return defaultHandler(accessor, data)
    }

    final override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: RaiseContext<D, E>): R = with(data.raise) {
        visitPropertyAccessor(accessor, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitPropertyGetter(getter: KSPropertyGetter, data: D): R {
        return defaultHandler(getter, data)
    }

    final override fun visitPropertyGetter(getter: KSPropertyGetter, data: RaiseContext<D, E>): R = with(data.raise) {
        visitPropertyGetter(getter, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitPropertySetter(setter: KSPropertySetter, data: D): R {
        return defaultHandler(setter, data)
    }

    final override fun visitPropertySetter(setter: KSPropertySetter, data: RaiseContext<D, E>): R = with(data.raise) {
        visitPropertySetter(setter, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitClassifierReference(reference: KSClassifierReference, data: D): R {
        return defaultHandler(reference, data)
    }

    final override fun visitClassifierReference(reference: KSClassifierReference, data: RaiseContext<D, E>): R = with(data.raise) {
        visitClassifierReference(reference, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitDefNonNullReference(reference: KSDefNonNullReference, data: D): R {
        return defaultHandler(reference, data)
    }

    final override fun visitDefNonNullReference(reference: KSDefNonNullReference, data: RaiseContext<D, E>): R = with(data.raise) {
        visitDefNonNullReference(reference, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitReferenceElement(element: KSReferenceElement, data: D): R {
        return defaultHandler(element, data)
    }

    final override fun visitReferenceElement(element: KSReferenceElement, data: RaiseContext<D, E>): R = with(data.raise) {
        visitReferenceElement(element, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitTypeAlias(typeAlias: KSTypeAlias, data: D): R {
        return defaultHandler(typeAlias, data)
    }

    final override fun visitTypeAlias(typeAlias: KSTypeAlias, data: RaiseContext<D, E>): R = with(data.raise) {
        visitTypeAlias(typeAlias, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitTypeArgument(typeArgument: KSTypeArgument, data: D): R {
        return defaultHandler(typeArgument, data)
    }

    final override fun visitTypeArgument(typeArgument: KSTypeArgument, data: RaiseContext<D, E>): R = with(data.raise) {
        visitTypeArgument(typeArgument, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: D): R {
        return defaultHandler(classDeclaration, data)
    }

    final override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: RaiseContext<D, E>): R = with(data.raise) {
        visitClassDeclaration(classDeclaration, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitTypeParameter(typeParameter: KSTypeParameter, data: D): R {
        return defaultHandler(typeParameter, data)
    }

    final override fun visitTypeParameter(typeParameter: KSTypeParameter, data: RaiseContext<D, E>): R = with(data.raise) {
        visitTypeParameter(typeParameter, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitTypeReference(typeReference: KSTypeReference, data: D): R {
        return defaultHandler(typeReference, data)
    }

    final override fun visitTypeReference(typeReference: KSTypeReference, data: RaiseContext<D, E>): R = with(data.raise) {
        visitTypeReference(typeReference, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitValueParameter(valueParameter: KSValueParameter, data: D): R {
        return defaultHandler(valueParameter, data)
    }

    final override fun visitValueParameter(valueParameter: KSValueParameter, data: RaiseContext<D, E>): R = with(data.raise) {
        visitValueParameter(valueParameter, data.data)
    }

    context(Raise<Nel<E>>)
    open fun visitValueArgument(valueArgument: KSValueArgument, data: D): R {
        return defaultHandler(valueArgument, data)
    }

    final override fun visitValueArgument(valueArgument: KSValueArgument, data: RaiseContext<D, E>): R = with(data.raise) {
        visitValueArgument(valueArgument, data.data)
    }
}

context(Raise<Nel<E>>)
internal fun <D, R, E> KSNode.accept(visitor: KSVisitor<RaiseContext<D, E>, R>, data: D): R {
    return accept(visitor, RaiseContext(data))
}
