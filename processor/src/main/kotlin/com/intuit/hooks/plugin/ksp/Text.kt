package com.intuit.hooks.plugin.ksp

import com.google.devtools.ksp.symbol.*

internal val KSTypeArgument.text: String get() = when (variance) {
    Variance.STAR -> variance.label
    // type should always be defined if not star projected
    Variance.INVARIANT -> type!!.text
    else -> "${variance.label} ${type!!.text}"
}

internal val List<KSTypeArgument>.text: String get() = if (isEmpty()) {
    ""
} else {
    "<${joinToString(transform = KSTypeArgument::text)}>"
}

internal val KSTypeReference.text: String get() = element?.let {
    when (it) {
        // Use lambda type shorthand
        is KSCallableReference -> "${if (this.modifiers.contains(Modifier.SUSPEND)) "suspend " else ""}(${
        it.functionParameters.map(KSValueParameter::type).joinToString(transform = KSTypeReference::text)
        }) -> ${it.returnType.text}"
        else -> "$it${it.typeArguments.text}"
    }
} ?: throw HooksProcessor.Exception("element was null, cannot translate KSTypeReference to code text: $this")
