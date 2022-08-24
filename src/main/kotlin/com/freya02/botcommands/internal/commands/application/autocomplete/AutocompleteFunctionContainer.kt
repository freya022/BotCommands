package com.freya02.botcommands.internal.commands.application.autocomplete

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler as AutocompleteHandlerAnnotation

@BService
internal class AutocompleteFunctionContainer(classPathContainer: ClassPathContainer) {
    private val autocompleteFunctions: Map<String, KFunction<Collection<*>>>

    init {
        autocompleteFunctions = classPathContainer.functionsWithAnnotation<AutocompleteHandlerAnnotation>()
            .requireNonStatic()
            .requireFirstArg(CommandAutoCompleteInteractionEvent::class)
            .map {
                val returnType = it.function.returnType
                ReflectionUtils.getCollectionReturnType(returnType.jvmErasure.java, returnType.javaType) ?: throwUser(
                    it.function,
                    "Autocomplete handler needs to return a Collection"
                )

                @Suppress("UNCHECKED_CAST")
                it.function as KFunction<Collection<*>>
            }
            .associateBy { it.findAnnotation<AutocompleteHandlerAnnotation>()!!.name }
    }

    operator fun get(handlerName: String): KFunction<Collection<*>>? = autocompleteFunctions[handlerName]
}