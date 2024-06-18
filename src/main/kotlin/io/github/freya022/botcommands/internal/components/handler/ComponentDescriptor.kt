package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.core.Logging.toUnwrappedLogger
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.parameters.toFallbackOptionBuilder
import io.github.freya022.botcommands.internal.transformParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.shortSignature
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation

internal class ComponentDescriptor internal constructor(
    context: BContextImpl,
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
                if (parameter.hasAnnotation<ComponentData>()) {
                    ComponentHandlerOptionBuilder(optionParameter)
                } else if (/* TODO remove */ context.serviceContainer.canCreateWrappedService(parameter) != null) {
                    // Fallback to component data if no service is found
                    function.declaringClass.java.toUnwrappedLogger()
                        .warn { "Component data parameter '$declaredName' must be annotated with ${annotationRef<ComponentData>()}, it will be enforced in a later release, in ${function.shortSignature}" }

                    if (resolverContainer.hasResolverOfType<ICustomResolver<*, *>>(parameter.wrap())) {
                        CustomOptionBuilder(optionParameter)
                    } else {
                        ComponentHandlerOptionBuilder(optionParameter)
                    }
                } else {
                    optionParameter.toFallbackOptionBuilder(context.serviceContainer, resolverContainer)
                }
            },
            aggregateBlock = { ComponentHandlerParameterImpl(context, it, eventType) }
        )
    }

    internal val optionSize = parameters.sumOf { p -> p.allOptions.count { o -> o.optionType == OptionType.OPTION } }
}