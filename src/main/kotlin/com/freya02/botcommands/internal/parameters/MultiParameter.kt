package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSingleAggregator
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

class MultiParameter(
    typeCheckingFunction: KFunction<*>,
    val typeCheckingParameterName: String,
    executableFunction: KFunction<*>,
    val executableParameterName: String
) {
    constructor(function: KFunction<*>, parameterName: String) : this(function, parameterName, function, parameterName)

    /**
     * **Note:** Can either be the user-defined aggregator or the command function
     *
     * See [AbstractOption.kParameter]
     */
    val typeCheckingFunction = typeCheckingFunction.reflectReference()

    /**
     * Can only be the (possibly non-user-defined) aggregator function
     */
    val executableFunction = executableFunction.reflectReference()

    val typeCheckingParameter = typeCheckingFunction.nonInstanceParameters.first { it.findDeclarationName() == typeCheckingParameterName }
    val executableParameter = executableFunction.nonInstanceParameters.first { it.findDeclarationName() == executableParameterName }

    init {
        if (typeCheckingFunction.isSingleAggregator()) {
            throwInternal("Type checking parameter should not belong to weakly types internal functions")
        }
    }

    fun withTypeCheckingParameterName(typeCheckingFunctionParameterName: String): MultiParameter {
        return MultiParameter(
            typeCheckingFunction, typeCheckingFunctionParameterName,
            executableFunction, executableParameterName
        )
    }
}