package com.freya02.botcommands.internal.commands.application.autocomplete

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

@BService
internal class AutocompleteHandlerContainer(classPathContainer: ClassPathContainer) {
    private val autocompleteFunctions: Map<String, ClassPathFunction>

    init {
        autocompleteFunctions = classPathContainer.functionsWithAnnotation<com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler>()
            .requireNonStatic()
            .requireFirstArg(CommandAutoCompleteInteractionEvent::class)
            .onEach {
                val returnType = it.function.returnType
                ReflectionUtils.getCollectionReturnType(returnType.jvmErasure.java, returnType.javaType) ?: throwUser(
                    it.function,
                    "Autocomplete handler needs to return a Collection"
                )
            }
            .associateBy { it.function.findAnnotation<com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler>()!!.name }
    }

    operator fun get(handlerName: String): ClassPathFunction? = autocompleteFunctions[handlerName]
}