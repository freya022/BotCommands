package com.freya02.botcommands.internal.commands.application.autocomplete

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.core.service.FunctionAnnotationsMap
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
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
                val autocompleteFunction = it.function as KFunction<Collection<*>>
                val autocompleteHandlerAnnotation = autocompleteFunction.findAnnotation<AutocompleteHandler>()!!

                AutocompleteInfoBuilder(context, autocompleteHandlerAnnotation.name, autocompleteFunction).apply {
                    mode = autocompleteHandlerAnnotation.mode
                    showUserInput = autocompleteHandlerAnnotation.showUserInput

                    autocompleteFunction.findAnnotation<CacheAutocomplete>()?.let { autocompleteCacheAnnotation ->
                        cache(autocompleteCacheAnnotation.cacheMode) {
                            cacheSize = autocompleteCacheAnnotation.cacheSize

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
