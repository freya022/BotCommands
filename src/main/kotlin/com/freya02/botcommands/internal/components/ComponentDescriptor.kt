package com.freya02.botcommands.internal.components

import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.core.reflection.MemberEventFunction
import com.freya02.botcommands.internal.parameters.OptionParameter
import com.freya02.botcommands.internal.transformParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.jvm.jvmErasure

class ComponentDescriptor(
    context: BContextImpl,
    override val function: MemberEventFunction<GenericComponentInteractionCreateEvent, *>
) : IExecutableInteractionInfo {
    override val parameters: List<ComponentHandlerParameter>

    init {
        parameters = function.nonInstanceParameters.drop(1).transformParameters(
            builderBlock = { function, parameter, declaredName ->
                val service = context.serviceContainer.peekServiceOrNull(parameter.type.jvmErasure)
                when {
                    service != null -> CustomOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                    else -> ComponentHandlerOptionBuilder(OptionParameter.fromSelfAggregate(function, declaredName))
                }
            },
            aggregateBlock = { ComponentHandlerParameter(context, it) }
        )
    }
}