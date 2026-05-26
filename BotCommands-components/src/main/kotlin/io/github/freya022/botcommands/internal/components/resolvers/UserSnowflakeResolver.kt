package io.github.freya022.botcommands.internal.components.resolvers

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.timeout.options.TimeoutOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Resolver
@ServiceName("componentUserSnowflakeResolver")
@RequiresComponents
internal class UserSnowflakeResolver :
        ClassParameterResolver<UserSnowflakeResolver, UserSnowflake>(UserSnowflake::class),
        ComponentParameterResolver<UserSnowflakeResolver, UserSnowflake>,
        TimeoutParameterResolver<UserSnowflakeResolver, UserSnowflake> {

    override fun serialize(obj: UserSnowflake) = SerializedComponentData.fromString(obj.id)

    override suspend fun resolveSuspend(option: ComponentOption, event: GenericComponentInteractionCreateEvent, data: SerializedComponentData): UserSnowflake =
        UserSnowflake.fromId(data.asString())

    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): UserSnowflake =
        UserSnowflake.fromId(data.asString())
}
