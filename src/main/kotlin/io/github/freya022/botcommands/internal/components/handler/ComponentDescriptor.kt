package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.commands.builder.ServiceOptionBuilder
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.MemberParamFunction
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.transformParameters
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

class ComponentDescriptor internal constructor(
    context: BContextImpl,
    override val eventFunction: MemberParamFunction<GenericComponentInteractionCreateEvent, *>
) : IExecutableInteractionInfo {
    override val parameters: List<ComponentHandlerParameter>

    init {
        parameters = eventFunction.transformParameters(
            builderBlock = { function, parameter, declaredName ->
                when (context.serviceContainer.canCreateWrappedService(parameter)) {
                    //No error => is a service
                    null -> ServiceOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                    else -> ComponentHandlerOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                }
            },
            aggregateBlock = { ComponentHandlerParameter(context, it) }
        )
    }

    internal val optionSize = parameters.sumOf { p -> p.allOptions.count { o -> o.optionType == OptionType.OPTION } }
}