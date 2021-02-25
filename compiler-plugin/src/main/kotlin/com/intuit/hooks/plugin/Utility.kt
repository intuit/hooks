package com.intuit.hooks.plugin

import arrow.core.NonEmptyList
import arrow.meta.phases.CompilerContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*

internal val KtClass.isHooksDslClass get() = hasHooksDslSupertype && containsHooksDslImport

private val KtClass.hasHooksDslSupertype get() = getSuperTypeList()?.entries?.any {
    it.text == "Hooks()"
} ?: false

private val KtClass.containsHooksDslImport get() = containingKtFile.importList?.imports?.any {
    it.text.contains("com.intuit.hooks.dsl.")
} ?: false

internal fun String.capitalizeFirstLetter(): String = this.mapIndexed { i, c -> if (i == 0) c.toUpperCase() else c }.joinToString(separator = "")

internal fun CompilerContext.reportHookErrors(invalid: NonEmptyList<HookValidationError>) =
    invalid.forEach { messageCollector?.report(it) }

private fun MessageCollector.report(hookValidationError: HookValidationError) =
    report(CompilerMessageSeverity.ERROR, hookValidationError.msg, hookValidationError.property)

private fun MessageCollector.report(severity: CompilerMessageSeverity, message: String, psiElement: PsiElement) {
    val location = CompilerMessageLocation.create(
        psiElement.containingFile.virtualFile.path,
        psiElement.textRangeInParent.startOffset, // todo: this might not be right...like at all?
        psiElement.textRangeInParent.endOffset,
        psiElement.text
    )
    this.report(severity, message, location)
}
