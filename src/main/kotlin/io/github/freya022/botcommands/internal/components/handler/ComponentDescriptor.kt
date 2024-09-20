package io.github.freya022.botcommands.internal.components.handler

import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.core.Logging.toUnwrappedLogger
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.components.handler.options.ComponentHandlerParameterImpl
import io.github.freya022.botcommands.internal.components.handler.options.builder.ComponentHandlerOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.options.builder.CustomOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.options.transformParameters
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.parameters.toFallbackOptionBuilder
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.shortSignature
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

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
                if (parameter.hasAnnotationRecursive<ComponentData>()) {
                    ComponentHandlerOptionBuilderImpl(optionParameter)
                } else if (/* TODO remove */ context.serviceContainer.canCreateWrappedService(parameter) != null) {
                    // Fallback to component data if no service is found
                    function.declaringClass.java.toUnwrappedLogger()
                        .warn { "Component data parameter '$declaredName' must be annotated with ${annotationRef<ComponentData>()}, it will be enforced in a later release, in ${function.shortSignature}" }

                    if (resolverContainer.hasResolverOfType<ICustomResolver<*, *>>(parameter.wrap())) {
                        CustomOptionBuilderImpl(optionParameter)
                    } else {
                        ComponentHandlerOptionBuilderImpl(optionParameter)
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