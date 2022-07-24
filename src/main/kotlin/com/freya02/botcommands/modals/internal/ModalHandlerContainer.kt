package com.freya02.botcommands.modals.internal

import com.freya02.botcommands.annotations.api.modals.annotations.ModalHandler
import com.freya02.botcommands.core.api.annotations.BService
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.requireFirstArg
import com.freya02.botcommands.core.internal.requireNonStatic
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.modals.InternalModals
import com.freya02.botcommands.internal.modals.ModalHandlerInfo
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

@BService
internal class ModalHandlerContainer(context: BContextImpl, classPathContainer: ClassPathContainer) {
    val handlers : MutableMap<String, ModalHandlerInfo> = hashMapOf()

    init {
        InternalModals.setContext(context)

        classPathContainer.functionsWithAnnotation<ModalHandler>()
            .requireNonStatic()
            .requireFirstArg(ModalInteractionEvent::class)
            .forEach {
                val handlerInfo = ModalHandlerInfo(context, it.instance, it.function)
                handlers[handlerInfo.handlerName] = handlerInfo
            }
    }

    operator fun get(handlerName: String): ModalHandlerInfo? = handlers[handlerName]
}