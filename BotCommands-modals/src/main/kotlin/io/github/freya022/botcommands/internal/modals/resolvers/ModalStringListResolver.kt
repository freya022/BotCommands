package io.github.freya022.botcommands.internal.modals.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.api.modals.options.ModalOption
import io.github.freya022.botcommands.api.parameters.TypedParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import kotlin.reflect.typeOf

@Resolver
@RequiresModals
internal class ModalStringListResolver :
        TypedParameterResolver<ModalStringListResolver, List<String>>(typeOf<List<String>>()),
        ModalParameterResolver<ModalStringListResolver, List<String>> {

    override suspend fun resolveSuspend(
        option: ModalOption,
        event: ModalEvent,
        modalMapping: ModalMapping,
    ): List<String> = modalMapping.asStringList
}
