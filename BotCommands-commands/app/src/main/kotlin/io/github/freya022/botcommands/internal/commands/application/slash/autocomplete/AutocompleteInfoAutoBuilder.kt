package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.cache.caffeineCache
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteHandlerProvider
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteManager
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import java.util.ServiceLoader
import kotlin.reflect.KFunction

@BService
@RequiresApplicationCommands
internal class AutocompleteInfoAutoBuilder internal constructor() : AutocompleteHandlerProvider {
    override fun declareAutocomplete(manager: AutocompleteManager) {
        val functionAnnotationsMap = manager.context.getService<FunctionAnnotationsMap>()
        val cacheBuilders = ServiceLoader.load(CacheBuilder::class.java).toList() + object : CacheBuilder {
            context(builder: AutocompleteInfoBuilder)
            override fun handle(function: KFunction<Collection<Any>>): Boolean {
                val annotation = function.findAnnotationRecursive<CacheAutocomplete>() ?: return false

                builder.caffeineCache {
                    forceCache = annotation.forceCache
                    cacheSize = annotation.cacheSize

                    compositeKeys = annotation.compositeKeys.toList()
                    userLocal = annotation.userLocal
                    channelLocal = annotation.channelLocal
                    guildLocal = annotation.guildLocal
                }

                return true
            }
        }

        functionAnnotationsMap.get<AutocompleteHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(CommandAutoCompleteInteractionEvent::class))
            .requiredFilter(FunctionFilter.returnType<Collection<Any>>(ignoreNullability = false))
            .forEach {
                @Suppress("UNCHECKED_CAST")
                val autocompleteFunction = it.function as KFunction<Collection<Any>>
                val autocompleteHandlerAnnotation = autocompleteFunction.findAnnotationRecursive<AutocompleteHandler>()!!

                manager.autocomplete(autocompleteFunction, autocompleteHandlerAnnotation.name.nullIfBlank()) {
                    declarationSite = DeclarationSite.fromFunctionSignature(autocompleteFunction)

                    mode = autocompleteHandlerAnnotation.mode
                    showUserInput = autocompleteHandlerAnnotation.showUserInput

                    // Apply cache from compatible annotations
                    for (cacheBuilder in cacheBuilders) {
                        if (cacheBuilder.handle(autocompleteFunction)) {
                            break
                        }
                    }
                }
            }
    }

    interface CacheBuilder {
        context(builder: AutocompleteInfoBuilder)
        fun handle(function: KFunction<Collection<Any>>): Boolean
    }
}
