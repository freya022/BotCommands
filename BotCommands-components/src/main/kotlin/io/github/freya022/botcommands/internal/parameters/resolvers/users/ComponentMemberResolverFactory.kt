package io.github.freya022.botcommands.internal.parameters.resolvers.users

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.core.entities.asInputUser
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Resolver
@RequiresComponents
internal class ComponentMemberResolver(
    private val resolver: ComponentInputUserResolver,
) : ClassParameterResolver<ComponentMemberResolver, Member>(Member::class),
    ComponentParameterResolver<ComponentMemberResolver, Member> {

    override fun serialize(obj: Member): SerializedComponentData {
        return resolver.serialize(obj.asInputUser())
    }

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): Member? {
        return resolver.resolveSuspend(option, event, data)?.member
    }
}
