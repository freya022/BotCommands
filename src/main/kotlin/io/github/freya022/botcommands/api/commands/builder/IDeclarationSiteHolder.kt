package io.github.freya022.botcommands.api.commands.builder

interface IDeclarationSiteHolder {
    /**
     * Purely for debugging purposes, will be shown in exceptions.
     *
     * Useful to know where something was declared to quickly redirect to the source of the exception.
     */
    val declarationSite: DeclarationSite?
}