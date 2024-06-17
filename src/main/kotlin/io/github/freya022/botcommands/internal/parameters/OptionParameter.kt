package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.api.commands.builder.ServiceOptionBuilder
import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.reflect.KFunction

internal class OptionParameter internal constructor(
    typeCheckingFunction: KFunction<*>,
    internal val typeCheckingParameterName: String,
    executableFunction: KFunction<*>,
    executableParameterName: String
) {
    internal val typeCheckingFunction = typeCheckingFunction.reflectReference()
    internal val typeCheckingParameter = this.typeCheckingFunction.nonInstanceParameters.find { it.findDeclarationName() == typeCheckingParameterName }
        ?: throwArgument(this.typeCheckingFunction, "Could not find a parameter named '$typeCheckingParameterName'")

    /**
     * Can only be the (possibly non-user-defined) aggregation function
     */
    internal val executableFunction = executableFunction.reflectReference()
    internal val executableParameter = this.executableFunction.nonInstanceParameters.first { it.findDeclarationName() == executableParameterName }

    internal companion object {
        internal fun fromSelfAggregate(commandFunction: KFunction<*>, parameterName: String) =
            SingleAggregatorParameter(commandFunction, parameterName).toOptionParameter(commandFunction, parameterName)
    }
}

internal fun OptionParameter.toFallbackOptionBuilder(
    serviceContainer: ServiceContainer,
    resolverContainer: ResolverContainer,
): OptionBuilder {
    // Better check for the resolver first,
    // as we can provide a more useful error message than if we let ResolverContainer throw one
    return if (resolverContainer.hasResolverOfType<ICustomResolver<*, *>>(typeCheckingParameter.wrap())) {
        CustomOptionBuilder(this)
    } else {
        val serviceError = serviceContainer.canCreateWrappedService(typeCheckingParameter)
        require(serviceError == null) {
            "No compatible resolver found, service loading also failed, check your handler docs for details\n${serviceError!!.toDetailedString()}"
        }

        ServiceOptionBuilder(this)
    }
}