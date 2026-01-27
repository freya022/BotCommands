package io.github.freya022.botcommands.internal.modals.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.options.ModalOption
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.interactions.modals.ModalMapping

@Resolver
internal object ModalStringResolver :
        ClassParameterResolver<ModalStringResolver, String>(String::class),
        ModalParameterResolver<ModalStringResolver, String> {

    override suspend fun resolveSuspend(
        option: ModalOption,
        event: ModalEvent,
        modalMapping: ModalMapping,
    ): String? {
        return when (modalMapping.type) {
            Component.Type.STRING_SELECT -> {
                val values = modalMapping.asStringList
                if (values.size > 1)
                    error("Cannot get a String from a string select menu with more than a single value")
                values.firstOrNull()
            }
            Component.Type.TEXT_INPUT, Component.Type.RADIO_GROUP -> when {
                option.isRequired -> modalMapping.asString
                else -> modalMapping.asOptionalString
            }
            else -> error("Cannot get a String from a ${modalMapping.type} input")
        }
    }
}
