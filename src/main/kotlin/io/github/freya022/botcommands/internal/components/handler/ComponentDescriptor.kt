package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.MemberEventFunction
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.transformParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.jvm.jvmErasure

class ComponentDescriptor internal constructor(
    context: BContextImpl,
    override val eventFunction: MemberEventFunction<GenericComponentInteractionCreateEvent, *>
) : IExecutableInteractionInfo {
    override val parameters: List<ComponentHandlerParameter>

    init {
        parameters = function.nonInstanceParameters.drop(1).transformParameters(
            builderBlock = { function, parameter, declaredName ->
                when (context.serviceContainer.canCreateService(parameter.type.jvmErasure)) {
                    //No error => is a service
                    null -> CustomOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                    else -> ComponentHandlerOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                }
            },
            aggregateBlock = { ComponentHandlerParameter(context, it) }
        )
    }

    internal val optionSize = parameters.sumOf { p -> p.allOptions.count { o -> o.optionType == OptionType.OPTION } }
}