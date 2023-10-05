package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toMemberEventFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

@BService
internal class ModalHandlerContainer(context: BContextImpl, functionAnnotationsMap: FunctionAnnotationsMap) {
    val handlers : MutableMap<String, ModalHandlerInfo> = hashMapOf()

    init {
        functionAnnotationsMap.getFunctionsWithAnnotation<ModalHandler>()
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