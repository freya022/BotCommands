package com.freya02.botcommands.internal.commands.application.autocomplete

import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.core.requireReturnType
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

@BService
internal class AutocompleteInfoContainer(classPathContainer: ClassPathContainer) {
    private val infoMap: Map<String, AutocompleteInfo>

    init {
        infoMap = classPathContainer.functionsWithAnnotation<AutocompleteHandler>()
            .requireNonStatic()
            .requireFirstArg(CommandAutoCompleteInteractionEvent::class)
            .requireReturnType(Collection::class)
            .associate {
                val returnType = it.function.returnType
                ReflectionUtils.getCollectionReturnType(returnType.jvmErasure.java, returnType.javaType) ?: throwUser(
                    it.function,
                    "Autocomplete handler needs to return a Collection"
                )

                @Suppress("UNCHECKED_CAST")
                val autocompleteFunction = it.function as KFunction<Collection<*>>
                val autocompleteHandlerAnnotation = autocompleteFunction.findAnnotation<AutocompleteHandler>()!!

                val info = AutocompleteInfoBuilder(autocompleteHandlerAnnotation.name).apply {
                    function = autocompleteFunction

                    mode = autocompleteHandlerAnnotation.mode
                    showUserInput = autocompleteHandlerAnnotation.showUserInput

                    autocompleteFunction.findAnnotation<CacheAutocomplete>()?.let { autocompleteCacheAnnotation ->
                        cache {
                            cacheMode = autocompleteCacheAnnotation.cacheMode
                            cacheSize = autocompleteCacheAnnotation.cacheSize

                            userLocal = autocompleteCacheAnnotation.userLocal
                            channelLocal = autocompleteCacheAnnotation.channelLocal
                            guildLocal = autocompleteCacheAnnotation.guildLocal
                        }
                    }
                }.build()

                autocompleteHandlerAnnotation.name to info
            }
    }

    operator fun get(handlerName: String): AutocompleteInfo? = infoMap[handlerName]
}