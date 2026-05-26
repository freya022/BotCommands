package io.github.freya022.botcommands.internal.components.resolvers

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Resolver
@ServiceName("componentGuildResolver")
@RequiresComponents
internal class GuildResolver :
        ClassParameterResolver<GuildResolver, Guild>(Guild::class),
        ComponentParameterResolver<GuildResolver, Guild> {

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): Guild? = event.jda.getGuildById(data.asString())

    override fun serialize(obj: Guild) = SerializedComponentData.fromString(obj.id)
}
