package io.github.freya022.botcommands.internal.modals.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.options.ModalOption
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import net.dv8tion.jda.api.interactions.modals.ModalMapping

@Resolver
internal object ModalBooleanResolver :
        ClassParameterResolver<ModalBooleanResolver, Boolean>(Boolean::class),
        ModalParameterResolver<ModalBooleanResolver, Boolean> {

    override suspend fun resolveSuspend(
        option: ModalOption,
        event: ModalEvent,
        modalMapping: ModalMapping,
    ): Boolean = modalMapping.asBoolean
}
