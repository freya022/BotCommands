package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.api.commands.builder.ServiceOptionBuilder
import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwUser
import kotlin.reflect.KFunction

class OptionParameter(
    typeCheckingFunction: KFunction<*>,
    override val typeCheckingParameterName: String,
    executableFunction: KFunction<*>,
    val executableParameterName: String
) : AggregatedParameter {
    override val typeCheckingFunction = typeCheckingFunction.reflectReference()
    override val typeCheckingParameter = this.typeCheckingFunction.nonInstanceParameters.find { it.findDeclarationName() == typeCheckingParameterName }
        ?: throwUser(this.typeCheckingFunction, "Could not find a parameter named '$typeCheckingParameterName'")

    /**
     * Can only be the (possibly non-user-defined) aggregator function
     */
    val executableFunction = executableFunction.reflectReference()
    val executableParameter = this.executableFunction.nonInstanceParameters.first { it.findDeclarationName() == executableParameterName }

    companion object {
        fun fromSelfAggregate(commandFunction: KFunction<*>, parameterName: String) =
            SingleAggregatorParameter(commandFunction, parameterName).toOptionParameter(commandFunction, parameterName)
    }
}

internal fun OptionParameter.toServiceOrCustomOptionBuilder(serviceContainer: ServiceContainer): OptionBuilder {
    return if (serviceContainer.canCreateWrappedService(typeCheckingParameter) == null) {
        ServiceOptionBuilder(this)
    } else {
        // Custom options being the fallback are important,
        // as if the parameter has no compatible resolver,
        // it will print both the required resolver AND the service error
        CustomOptionBuilder(this)
    }
}