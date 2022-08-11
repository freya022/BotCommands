package com.freya02.botcommands.commands.internal.application.autocomplete

import com.freya02.botcommands.annotations.api.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.ClassPathFunction
import com.freya02.botcommands.core.internal.requireFirstArg
import com.freya02.botcommands.core.internal.requireNonStatic
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
        autocompleteFunctions = classPathContainer.functionsWithAnnotation<AutocompleteHandler>()
            .requireNonStatic()
            .requireFirstArg(CommandAutoCompleteInteractionEvent::class)
            .onEach {
                val returnType = it.function.returnType
                ReflectionUtils.getCollectionReturnType(returnType.jvmErasure.java, returnType.javaType) ?: throwUser(
                    it.function,
                    "Autocomplete handler needs to return a Collection"
                )
            }
            .associateBy { it.function.findAnnotation<AutocompleteHandler>()!!.name }
    }

    operator fun get(handlerName: String): ClassPathFunction? = autocompleteFunctions[handlerName]
}