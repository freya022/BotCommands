package io.github.freya022.botcommands.internal.parameters.resolvers.users

import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.core.entities.asInputUser
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Resolver
internal class ComponentUserResolver(
    private val resolver: ComponentInputUserResolver,
) : ClassParameterResolver<ComponentUserResolver, User>(User::class),
    ComponentParameterResolver<ComponentUserResolver, User> {

    override fun serialize(obj: User): SerializedComponentData {
        return resolver.serialize(obj.asInputUser())
    }

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): User? {
        return resolver.resolveSuspend(option, event, data)
    }
}
