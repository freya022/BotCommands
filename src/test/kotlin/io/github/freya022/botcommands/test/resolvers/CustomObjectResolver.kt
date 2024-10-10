package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.test.CustomObject
import net.dv8tion.jda.api.events.Event

@Resolver
class CustomObjectResolver :
    ClassParameterResolver<CustomObjectResolver, CustomObject>(CustomObject::class),
    ICustomResolver<CustomObjectResolver, CustomObject> {

    override suspend fun resolveSuspend(option: Option, event: Event): CustomObject {
        return CustomObject()
    }
}