package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.modals.annotations.ModalHandler
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.reflection.toMemberEventFunction
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

@BService
internal class ModalHandlerContainer(context: BContextImpl, classPathContainer: ClassPathContainer) {
    val handlers : MutableMap<String, ModalHandlerInfo> = hashMapOf()

    init {
        classPathContainer.functionsWithAnnotation<ModalHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(ModalInteractionEvent::class))
            .forEach {
                val handlerInfo = ModalHandlerInfo(context, it.toMemberEventFunction())
                val oldHandler = handlers.put(handlerInfo.handlerName, handlerInfo)

                if (oldHandler != null) {
                    throwUser("Tried to register modal handler '%s' at %s but it was already registered at %s".format(
                        handlerInfo.handlerName,
                        handlerInfo.function.shortSignature,
                        oldHandler.function.shortSignature
                    ))
                }
            }
    }

    operator fun get(handlerName: String): ModalHandlerInfo? = handlers[handlerName]
}