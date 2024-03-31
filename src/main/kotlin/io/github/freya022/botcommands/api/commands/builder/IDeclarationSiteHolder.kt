package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.findCaller

interface IDeclarationSiteHolder {
    /**
     * Purely for debugging purposes, will be shown in exceptions.
     *
     * Useful to know where something was declared to quickly redirect to the source of the exception.
     */
    val declarationSite: DeclarationSite
}

interface IDeclarationSiteHolderBuilder : IDeclarationSiteHolder {
    override var declarationSite: DeclarationSite
}

private val StackWalker.StackFrame.sourceFile: String
    get() = fileName ?: "${declaringClass.canonicalName.split('.').first { it.any(Char::isUpperCase) }}.java"

internal fun <T : IDeclarationSiteHolderBuilder> T.setCallerAsDeclarationSite(): T {
    declarationSite = DeclarationSite.fromRaw(findCaller().let { frame ->
        "${frame.declaringClass.simpleNestedName}.${frame.methodName.substringBefore('$')} (${frame.sourceFile}:${frame.lineNumber})"
    })

    return this
}