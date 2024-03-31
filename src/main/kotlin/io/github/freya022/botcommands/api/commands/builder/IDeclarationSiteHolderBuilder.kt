@file:IgnoreStackFrame

package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.findCaller

interface IDeclarationSiteHolderBuilder : IDeclarationSiteHolder {
    override var declarationSite: DeclarationSite?
}

private val StackWalker.StackFrame.sourceFile: String
    get() = fileName ?: "${declaringClass.canonicalName.split('.').first { it.any(Char::isUpperCase) }}.java"

internal fun <T : IDeclarationSiteHolderBuilder> T.setCallerAsDeclarationSite(): T = apply {
    declarationSite = DeclarationSite.fromRaw(findCaller().let { frame ->
        "${frame.declaringClass.simpleNestedName}.${frame.methodName.substringBefore('$')} (${frame.sourceFile}:${frame.lineNumber})"
    })
}