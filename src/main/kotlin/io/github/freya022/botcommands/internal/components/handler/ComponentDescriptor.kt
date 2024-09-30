package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.components.handler.options.ComponentHandlerParameterImpl
import io.github.freya022.botcommands.internal.components.handler.options.builder.ComponentHandlerOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.options.transformParameters
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.parameters.toFallbackOptionBuilder
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

internal class ComponentDescriptor internal constructor(
    override val context: BContextImpl,
    handler: KFunction<*>,
    eventType: KClass<out GenericComponentInteractionCreateEvent>
) : ExecutableMixin {
    override val eventFunction = handler.toMemberParamFunction(context, eventType)
    override val parameters: List<ComponentHandlerParameterImpl>

    init {
        val resolverContainer = context.getService<ResolverContainer>()
        parameters = eventFunction.transformParameters(
            builderBlock = { function, parameter, declaredName ->
                val optionParameter = OptionParameter.fromSelfAggregate(function, declaredName)
                if (parameter.hasAnnotationRecursive<ComponentData>()) {
                    ComponentHandlerOptionBuilderImpl(optionParameter)
                } else {
                    optionParameter.toFallbackOptionBuilder(context.serviceContainer, resolverContainer)
                }
            },
            aggregateBlock = { ComponentHandlerParameterImpl(this, it, eventType) }
        )
    }

    internal val optionSize = parameters.sumOf { p -> p.allOptions.count { o -> o.optionType == OptionType.OPTION } }

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("For removal, confusing on whether it searches nested parameters, prefer using collection operations on 'parameters' instead, make an extension or an utility method")
    override fun getParameter(declaredName: String): AggregatedParameter? =
        parameters.find { it.name == declaredName }
}