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
@ServiceName("componentIntegerResolver")
@RequiresComponents
internal class IntegerResolver : ClassParameterResolver<IntegerResolver, Int>(Int::class),
                                 ComponentParameterResolver<IntegerResolver, Int>,
                                 TimeoutParameterResolver<IntegerResolver, Int> {

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): Int = data.asString().toInt()

    override fun serialize(obj: Int) = SerializedComponentData.fromString(obj.toString())


    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): Int =
        data.asString().toInt()
}
