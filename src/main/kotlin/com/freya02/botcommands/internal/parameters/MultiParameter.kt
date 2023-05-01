package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.AbstractOption
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSingleAggregator
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

class MultiParameter private constructor(
    typeCheckingFunction: KFunction<*>,
    val typeCheckingParameterName: String,
    executableFunction: KFunction<*>,
    val executableParameterName: String
) {
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

    val typeCheckingParameter = this.typeCheckingFunction.nonInstanceParameters.first { it.findDeclarationName() == typeCheckingParameterName }
    val executableParameter = this.executableFunction.nonInstanceParameters.first { it.findDeclarationName() == executableParameterName }

    init {
        if (this.typeCheckingFunction.isSingleAggregator()) {
            throwInternal("Type checking parameter should not belong to weakly types internal functions")
        }
    }

    fun toOptionParameter(optionFunction: KFunction<*>, parameterName: String) = when {
        //Keep the command's parameter for type checking if the internal aggregator is currently used
        this.executableFunction.isSingleAggregator() -> this
        //When the aggregator is user-defined
        else -> MultiParameter(optionFunction, parameterName, optionFunction, parameterName)
    }

    internal companion object {
        fun fromUserAggregate(aggregator: KFunction<*>, parameterName: String) =
            MultiParameter(aggregator, parameterName, aggregator, parameterName)

        fun fromSelfAggregate(commandFunction: KFunction<*>, parameterName: String) =
            MultiParameter(commandFunction, parameterName, OptionAggregateBuildersImpl.theSingleAggregator, "it")
    }
}