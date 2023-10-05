package io.github.freya022.botcommands.internal.commands.application.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import io.github.freya022.botcommands.internal.utils.requireUser
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

@BService
internal class AutocompleteInfoContainer(private val context: BContextImpl, functionAnnotationsMap: FunctionAnnotationsMap) {
    private val infoMap: Map<String, AutocompleteInfo>

    init {
        infoMap = functionAnnotationsMap.getFunctionsWithAnnotation<AutocompleteHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(CommandAutoCompleteInteractionEvent::class))
            .requiredFilter(FunctionFilter.returnType(Collection::class))
            .map {
                requireUser(it.function.returnType.jvmErasure.isSubclassOf(Collection::class), it.function) {
                    "Autocomplete handler needs to return a Collection"
                }

                @Suppress("UNCHECKED_CAST")
                val autocompleteFunction = it.function as KFunction<Collection<Any>>
                val autocompleteHandlerAnnotation = autocompleteFunction.findAnnotation<AutocompleteHandler>()!!

                AutocompleteInfoBuilder(context, autocompleteHandlerAnnotation.name, autocompleteFunction).apply {
                    mode = autocompleteHandlerAnnotation.mode
                    showUserInput = autocompleteHandlerAnnotation.showUserInput

                    autocompleteFunction.findAnnotation<CacheAutocomplete>()?.let { autocompleteCacheAnnotation ->
                        cache(autocompleteCacheAnnotation.cacheMode) {
                            forceCache = autocompleteCacheAnnotation.forceCache
                            cacheSize = autocompleteCacheAnnotation.cacheSize

                            compositeKeys = autocompleteCacheAnnotation.compositeKeys.toList()
                            userLocal = autocompleteCacheAnnotation.userLocal
                            channelLocal = autocompleteCacheAnnotation.channelLocal
                            guildLocal = autocompleteCacheAnnotation.guildLocal
                        }
                    }
                }.build()
            }
            .also {
                it.forEach { autocompleteInfo ->
                    val otherInfo = it.find { otherInfo -> otherInfo.name == autocompleteInfo.name && otherInfo !== autocompleteInfo }
                    if (otherInfo != null) //must be a duplicate
                        throw IllegalArgumentException("Autocomplete handler ${autocompleteInfo.name} is already registered at ${autocompleteInfo.function.shortSignatureNoSrc} and at ${otherInfo.function.shortSignatureNoSrc}")
                }
            }
            .associateBy { it.name }
    }

    operator fun get(handlerName: String): AutocompleteInfo? = infoMap[handlerName]
}
