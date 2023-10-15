package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.ICustomResolver
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.test.CustomObject
import net.dv8tion.jda.api.events.Event

@Resolver
class CustomObjectResolver :
    ClassParameterResolver<CustomObjectResolver, CustomObject>(CustomObject::class),
    ICustomResolver<CustomObjectResolver, CustomObject> {

    override suspend fun resolveSuspend(
        executableInteractionInfo: IExecutableInteractionInfo,
        event: Event
    ): CustomObject {
        return CustomObject()
    }
}