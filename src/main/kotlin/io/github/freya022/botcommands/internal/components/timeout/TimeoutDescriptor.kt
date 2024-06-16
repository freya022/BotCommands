package io.github.freya022.botcommands.internal.components.timeout

import io.github.freya022.botcommands.api.commands.builder.ServiceOptionBuilder
import io.github.freya022.botcommands.api.components.annotations.TimeoutData
import io.github.freya022.botcommands.api.core.Logging.toUnwrappedLogger
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.MemberParamFunction
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.parameters.OptionParameter
import io.github.freya022.botcommands.internal.transformParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.shortSignature
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

internal class TimeoutDescriptor<T : Any> internal constructor(
    context: BContextImpl,
    override val eventFunction: MemberParamFunction<T, *>,
    aggregatorFirstParamType: KClass<T>
) : ExecutableMixin {
    override val parameters: List<TimeoutHandlerParameter>

    init {
        parameters = eventFunction.transformParameters(
            builderBlock = { function, parameter, declaredName ->
                val optionParameter = OptionParameter.fromSelfAggregate(function, declaredName)
                if (parameter.hasAnnotation<TimeoutData>()) {
                    TimeoutHandlerOptionBuilder(optionParameter)
                } else if (/* TODO remove */ context.serviceContainer.canCreateWrappedService(parameter) != null) {
                    // Fallback to timeout data if no service is found
                    function.declaringClass.java.toUnwrappedLogger()
                        .warn { "Timeout data parameter '$declaredName' must be annotated with ${annotationRef<TimeoutData>()}, it will be enforced in a later release, in ${function.shortSignature}" }

                    TimeoutHandlerOptionBuilder(optionParameter)
                } else {
                    val serviceError = context.serviceContainer.canCreateWrappedService(parameter)
                    require(serviceError == null) {
                        "Could not get service parameter for '$declaredName', in ${function.shortSignature}, did you forget to use ${annotationRef<TimeoutData>()}?\n${serviceError!!.toDetailedString()}"
                    }

                    ServiceOptionBuilder(optionParameter)
                }
            },
            aggregateBlock = { TimeoutHandlerParameter(context, it, aggregatorFirstParamType) }
        )
    }

    internal val optionSize = parameters.sumOf { p -> p.allOptions.count { o -> o.optionType == OptionType.OPTION } }
}