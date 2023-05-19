package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSpecialAggregator
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

internal class VarargAggregatorParameter(
    commandFunction: KFunction<*>,
    parameterName: String
) : AggregatorParameter {
    override val typeCheckingFunction = commandFunction.reflectReference()
    override val typeCheckingParameterName = parameterName
    override val typeCheckingParameter = this.typeCheckingFunction.nonInstanceParameters.first { it.findDeclarationName() == parameterName }

    init {
        if (this.typeCheckingFunction.isSpecialAggregator()) {
            throwInternal("Tried to use a special aggregator in a ${javaClass.simpleNestedName}: ${this.typeCheckingFunction}")
        }

        requireUser(typeCheckingParameter.type.jvmErasure == List::class, typeCheckingFunction) {
            "Vararg parameter '$typeCheckingParameterName' must be a List"
        }
    }

    //Keep the command's parameter for type checking if the internal aggregator is currently used
    override fun toOptionParameter(optionFunction: KFunction<*>, parameterName: String) = when (parameterName) {
        //Using the parameter name is safe as both the aggregator and the declaration use the same names
        //  Use command parameter type as it is unknown
        "args" -> OptionParameter(typeCheckingFunction, typeCheckingParameterName, OptionAggregateBuildersImpl.theVarargAggregator, parameterName)
        else -> throwInternal("Unknown internal vararg parameter: $parameterName")
    }
}