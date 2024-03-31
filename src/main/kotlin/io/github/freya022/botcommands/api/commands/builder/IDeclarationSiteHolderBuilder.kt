@file:IgnoreStackFrame

package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import io.github.freya022.botcommands.api.core.utils.simpleNestedName

interface IDeclarationSiteHolderBuilder : IDeclarationSiteHolder {
    override var declarationSite: DeclarationSite?
}

private val stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

private val StackWalker.StackFrame.sourceFile: String
    get() = fileName ?: "${declaringClass.canonicalName.split('.').first { it.any(Char::isUpperCase) }}.java"

internal fun <T : IDeclarationSiteHolderBuilder> T.setCallerAsDeclarationSite(): T = apply {
    declarationSite = DeclarationSite.fromRaw(stackWalker.walk { stream ->
        val frame = stream
            .filter { !it.declaringClass.isAnnotationPresent(IgnoreStackFrame::class.java) }
            .findFirst().get()
        "${frame.declaringClass.simpleNestedName}.${frame.methodName.substringBefore('$')} (${frame.sourceFile}:${frame.lineNumber})"
    })
}