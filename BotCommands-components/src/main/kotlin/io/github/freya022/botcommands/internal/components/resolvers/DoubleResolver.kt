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
@ServiceName("componentDoubleResolver")
@RequiresComponents
internal class DoubleResolver : ClassParameterResolver<DoubleResolver, Double>(Double::class),
                                ComponentParameterResolver<DoubleResolver, Double>,
                                TimeoutParameterResolver<DoubleResolver, Double> {

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): Double {
        return data.asString().toDouble()
    }

    override fun serialize(obj: Double) = SerializedComponentData.fromString(obj.toString())


    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): Double {
        return data.asString().toDouble()
    }
}
