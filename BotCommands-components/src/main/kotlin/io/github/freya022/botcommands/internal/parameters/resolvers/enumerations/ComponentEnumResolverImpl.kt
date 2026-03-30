package io.github.freya022.botcommands.internal.parameters.resolvers.enumerations

import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.timeout.options.TimeoutOption
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal class ComponentEnumResolverImpl<E : Enum<E>> internal constructor(
    enumType: Class<E>,
) : ClassParameterResolver<ComponentEnumResolverImpl<E>, E>(enumType),
    ComponentParameterResolver<ComponentEnumResolverImpl<E>, E>,
    TimeoutParameterResolver<ComponentEnumResolverImpl<E>, E> {

    private val enumMap: Map<String, E> = enumType.enumConstants.associateBy { it.name }

    override fun serialize(obj: E) = SerializedComponentData.fromString(obj.name)

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): E? {
        return getEnumValueOrNull(data.asString())
    }

    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): E? =
        getEnumValueOrNull(data.asString())

    private fun getEnumValueOrNull(name: String): E? = enumMap[name.lowercase()]
}
