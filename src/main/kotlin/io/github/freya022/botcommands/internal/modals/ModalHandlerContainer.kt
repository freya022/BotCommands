package io.github.freya022.botcommands.internal.modals

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwUser

@BService
internal class ModalHandlerContainer(context: BContextImpl, functionAnnotationsMap: FunctionAnnotationsMap) {
    val handlers : MutableMap<String, ModalHandlerInfo> = hashMapOf()

    init {
        functionAnnotationsMap.get<ModalHandler>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(ModalEvent::class))
            .forEach {
                val handlerInfo = ModalHandlerInfo(context, it.toMemberParamFunction())
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