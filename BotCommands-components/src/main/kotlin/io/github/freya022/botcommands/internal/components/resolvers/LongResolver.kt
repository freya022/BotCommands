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
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Resolver
@ServiceName("componentLongResolver")
@RequiresComponents
internal class LongResolver : ClassParameterResolver<LongResolver, Long>(Long::class),
                              ComponentParameterResolver<LongResolver, Long>,
                              TimeoutParameterResolver<LongResolver, Long> {

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): Long = data.asString().toLong()

    override fun serialize(obj: Long) = SerializedComponentData.fromString(obj.toString())


    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): Long =
        data.asString().toLong()
}
