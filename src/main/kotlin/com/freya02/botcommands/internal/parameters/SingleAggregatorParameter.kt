package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSingleAggregator
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

internal class SingleAggregatorParameter(
    commandFunction: KFunction<*>,
    parameterName: String
) : AggregatorParameter {
    override val typeCheckingFunction = commandFunction.reflectReference()
    override val typeCheckingParameterName = parameterName
    override val typeCheckingParameter = this.typeCheckingFunction.nonInstanceParameters.first { it.findDeclarationName() == parameterName }

    init {
        if (this.typeCheckingFunction.isSingleAggregator()) {
            throwInternal("Tried to use something else than the internal single aggregator")
        }
    }

    //Keep the command's parameter for type checking if the internal aggregator is currently used
    override fun toOptionParameter(optionFunction: KFunction<*>, parameterName: String) =
        OptionParameter(typeCheckingFunction, typeCheckingParameterName, OptionAggregateBuildersImpl.theSingleAggregator, "it")
}