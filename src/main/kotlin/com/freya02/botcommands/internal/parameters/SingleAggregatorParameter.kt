package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.core.options.builder.InternalAggregators
import com.freya02.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

internal class SingleAggregatorParameter(
    commandFunction: KFunction<*>,
    parameterName: String
) : AggregatorParameter {
    override val typeCheckingFunction = commandFunction.reflectReference()
    override val typeCheckingParameterName = parameterName
    override val typeCheckingParameter = this.typeCheckingFunction.nonInstanceParameters.find { it.findDeclarationName() == parameterName }
        ?: throwUser(this.typeCheckingFunction, "Could not find a parameter named '$parameterName'")

    init {
        if (this.typeCheckingFunction.isSpecialAggregator()) {
            throwInternal("Tried to use a special aggregator in a ${javaClass.simpleNestedName}: ${this.typeCheckingFunction}")
        }
    }

    //Keep the command's parameter for type checking if the internal aggregator is currently used
    override fun toOptionParameter(optionFunction: KFunction<*>, parameterName: String) =
        OptionParameter(typeCheckingFunction, typeCheckingParameterName, InternalAggregators.theSingleAggregator, "it")
}