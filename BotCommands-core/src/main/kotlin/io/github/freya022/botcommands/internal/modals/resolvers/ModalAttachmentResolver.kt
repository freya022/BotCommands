package io.github.freya022.botcommands.internal.modals.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.options.ModalOption
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.modals.ModalMapping

@Resolver
internal object ModalAttachmentResolver :
        ClassParameterResolver<ModalAttachmentResolver, Message.Attachment>(Message.Attachment::class),
        ModalParameterResolver<ModalAttachmentResolver, Message.Attachment> {

    override suspend fun resolveSuspend(
        option: ModalOption,
        event: ModalEvent,
        modalMapping: ModalMapping,
    ): Message.Attachment? {
        return when (modalMapping.type) {
            Component.Type.FILE_UPLOAD -> {
                val values = modalMapping.asAttachmentList
                if (values.size > 1)
                    error("Cannot get an Attachment from a file upload with more than a single value")
                values.firstOrNull()
            }
            else -> error("Cannot get an Attachment from a ${modalMapping.type} input")
        }
    }
}
