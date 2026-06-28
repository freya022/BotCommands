package io.github.freya022.botcommands.internal.modals.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.options.ModalOption
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.throwArgument
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
            Component.Type.STRING_SELECT, Component.Type.CHECKBOX_GROUP -> {
                val values = modalMapping.asStringList
                if (values.size > 1) {
                    throwArgument(
                        option.kParameter.function,
                        "Cannot get a String from a ${modalMapping.type} with more than a single value"
                    )
                }
                values.firstOrNull()
            }
            Component.Type.TEXT_INPUT -> when {
                // When non-null and required, empty inputs pass empty strings
                option.isRequired -> modalMapping.asString
                // When null or optional, empty inputs pass null (which is also the same as missing parameter)
                else -> modalMapping.asOptionalString
            }
            Component.Type.RADIO_GROUP -> modalMapping.asOptionalString
            else -> error("Cannot get a String from a ${modalMapping.type} input")
        }
    }
}
