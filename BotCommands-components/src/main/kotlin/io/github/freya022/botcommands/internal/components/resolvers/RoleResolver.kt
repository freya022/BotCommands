package io.github.freya022.botcommands.internal.components.resolvers

import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Resolver
@ServiceName("componentRoleResolver")
internal class RoleResolver :
        ClassParameterResolver<RoleResolver, Role>(Role::class),
        ComponentParameterResolver<RoleResolver, Role> {

    override fun serialize(obj: Role) = SerializedComponentData.fromString(obj.id)

    override suspend fun resolveSuspend(option: ComponentOption, event: GenericComponentInteractionCreateEvent, data: SerializedComponentData): Role? {
        val guild = event.guild
        requireNotNull(guild) { "Can't get a role from DMs" }

        return guild.getRoleById(data.asString())
    }
}
