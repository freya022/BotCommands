package com.freya02.botcommands.internal.modals

import com.freya02.botcommands.annotations.api.modals.annotations.ModalHandler
import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.ClassPathContainer
import com.freya02.botcommands.internal.core.requireFirstArg
import com.freya02.botcommands.internal.core.requireNonStatic
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

@BService
internal class ModalHandlerContainer(context: BContextImpl, classPathContainer: ClassPathContainer) {
    val handlers : MutableMap<String, ModalHandlerInfo> = hashMapOf()

    init {
        classPathContainer.functionsWithAnnotation<ModalHandler>()
            .requireNonStatic()
            .requireFirstArg(ModalInteractionEvent::class)
            .forEach {
                val handlerInfo = ModalHandlerInfo(context, it.instance, it.function)
                val oldHandler = handlers.put(handlerInfo.handlerName, handlerInfo)

                if (oldHandler != null) {
                    throwUser("Tried to register modal handler '%s' at %s but it was already registered at %s".format(
                        handlerInfo.handlerName,
                        handlerInfo.method.shortSignature,
                        oldHandler.method.shortSignature
                    ))
                }
            }
    }

    operator fun get(handlerName: String): ModalHandlerInfo? = handlers[handlerName]
}