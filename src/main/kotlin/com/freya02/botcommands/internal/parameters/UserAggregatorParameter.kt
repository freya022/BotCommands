package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSpecialAggregator
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

internal class UserAggregatorParameter(
    typeCheckingFunction: KFunction<*>,
    override val typeCheckingParameterName: String
) : AggregatorParameter {
    override val typeCheckingFunction = typeCheckingFunction.reflectReference()
    override val typeCheckingParameter = this.typeCheckingFunction.nonInstanceParameters.find { it.findDeclarationName() == typeCheckingParameterName }
        ?: throwUser(this.typeCheckingFunction, "Could not find a parameter named '$typeCheckingParameterName'")

    init {
        if (this.typeCheckingFunction.isSpecialAggregator()) {
            throwInternal("Tried to use a special aggregator in a ${javaClass.simpleNestedName}: ${this.typeCheckingFunction}")
        }
    }

    //When the aggregator is user-defined
    override fun toOptionParameter(optionFunction: KFunction<*>, parameterName: String) =
        OptionParameter(optionFunction, parameterName, optionFunction, parameterName)
}