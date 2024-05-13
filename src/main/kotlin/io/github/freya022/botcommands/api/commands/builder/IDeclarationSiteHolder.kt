package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.internal.utils.StackSensitive
import io.github.freya022.botcommands.internal.utils.findCaller
import io.github.freya022.botcommands.internal.utils.toSignature

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

@OptIn(StackSensitive::class)
internal fun <T : IDeclarationSiteHolderBuilder> T.setCallerAsDeclarationSite(): T {
    declarationSite = DeclarationSite.fromRaw(findCaller(1).toSignature())

    return this
}