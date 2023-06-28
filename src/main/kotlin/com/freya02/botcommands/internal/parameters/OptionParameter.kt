package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import com.freya02.botcommands.internal.utils.findDeclarationName
import com.freya02.botcommands.internal.utils.throwUser
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