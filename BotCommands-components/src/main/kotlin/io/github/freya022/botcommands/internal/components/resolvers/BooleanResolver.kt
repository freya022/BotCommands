package io.github.freya022.botcommands.internal.components.resolvers

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
@ServiceName("componentBooleanResolver")
internal class BooleanResolver : ClassParameterResolver<BooleanResolver, Boolean>(Boolean::class),
                                 ComponentParameterResolver<BooleanResolver, Boolean>,
                                 TimeoutParameterResolver<BooleanResolver, Boolean> {

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): Boolean? = parseBoolean(data.asString())

    override fun serialize(obj: Boolean) = SerializedComponentData.fromString(obj.toString())


    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): Boolean? =
        parseBoolean(data.asString())


    private fun parseBoolean(arg: String): Boolean? {
        return if (arg.equals("false", ignoreCase = true)) {
            false
        } else if (arg.equals("true", ignoreCase = true)) {
            true
        } else {
            null
        }
    }
}
