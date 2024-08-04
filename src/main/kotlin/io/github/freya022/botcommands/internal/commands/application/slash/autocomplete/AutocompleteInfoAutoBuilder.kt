package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteHandlerProvider
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteManager
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.nullIfBlank
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@BService
@RequiresApplicationCommands
internal class AutocompleteInfoAutoBuilder internal constructor() : AutocompleteHandlerProvider {
    override fun declareAutocomplete(manager: AutocompleteManager) {
        val functionAnnotationsMap = manager.context.getService<FunctionAnnotationsMap>()

        functionAnnotationsMap.get<AutocompleteHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(CommandAutoCompleteInteractionEvent::class))
            .requiredFilter(FunctionFilter.returnType<Collection<Any>>(ignoreNullability = false))
            .forEach {
                @Suppress("UNCHECKED_CAST")
                val autocompleteFunction = it.function as KFunction<Collection<Any>>
                val autocompleteHandlerAnnotation = autocompleteFunction.findAnnotation<AutocompleteHandler>()!!

                manager.autocomplete(autocompleteFunction, autocompleteHandlerAnnotation.name.nullIfBlank()) {
                    declarationSite = DeclarationSite.fromFunctionSignature(autocompleteFunction)

                    mode = autocompleteHandlerAnnotation.mode
                    showUserInput = autocompleteHandlerAnnotation.showUserInput

                    autocompleteFunction.findAnnotation<CacheAutocomplete>()?.let { autocompleteCacheAnnotation ->
                        cache {
                            forceCache = autocompleteCacheAnnotation.forceCache
                            cacheSize = autocompleteCacheAnnotation.cacheSize

                            compositeKeys = autocompleteCacheAnnotation.compositeKeys.toList()
                            userLocal = autocompleteCacheAnnotation.userLocal
                            channelLocal = autocompleteCacheAnnotation.channelLocal
                            guildLocal = autocompleteCacheAnnotation.guildLocal
                        }
                    }
                }
            }
    }
}