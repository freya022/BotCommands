package io.github.freya022.botcommands.test_kt.resolvers

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ICustomResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.test_kt.CustomObject
import net.dv8tion.jda.api.events.Event

@Resolver
class CustomObjectResolver :
    ParameterResolver<CustomObjectResolver, CustomObject>(CustomObject::class),
    ICustomResolver<CustomObjectResolver, CustomObject> {

    override suspend fun resolveSuspend(
        context: BContext,
        executableInteractionInfo: IExecutableInteractionInfo,
        event: Event
    ): CustomObject {
        return CustomObject()
    }
}