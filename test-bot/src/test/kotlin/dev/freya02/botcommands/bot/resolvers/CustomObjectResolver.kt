package dev.freya02.botcommands.bot.resolvers

import dev.freya02.botcommands.bot.CustomObject
import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import net.dv8tion.jda.api.events.Event

@Resolver
class CustomObjectResolver :
    ClassParameterResolver<CustomObjectResolver, CustomObject>(CustomObject::class),
    ICustomResolver<CustomObjectResolver, CustomObject> {

    override suspend fun resolveSuspend(option: Option, event: Event): CustomObject {
        return CustomObject()
    }
}
