package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

internal class VarargAggregatorParameter internal constructor(
    commandFunction: KFunction<*>,
    parameterName: String
) : AggregatorParameter {
    override val typeCheckingFunction = commandFunction.reflectReference()
    override val typeCheckingParameterName = parameterName
    override val typeCheckingParameter = this.typeCheckingFunction.nonInstanceParameters.find { it.findDeclarationName() == parameterName }
        ?: throwArgument(this.typeCheckingFunction, "Could not find a parameter named '$parameterName'")

    init {
        if (this.typeCheckingFunction.isSpecialAggregator()) {
            throwInternal("Tried to use a special aggregator in a ${javaClass.simpleNestedName}: ${this.typeCheckingFunction}")
        }

        requireAt(typeCheckingParameter.type.jvmErasure == List::class, typeCheckingFunction) {
            "Vararg parameter '$typeCheckingParameterName' must be a List"
        }
    }

    //Keep the command's parameter for type checking if the internal aggregator is currently used
    override fun toOptionParameter(optionFunction: KFunction<*>, parameterName: String) = when (parameterName) {
        //Using the parameter name is safe as both the aggregator and the declaration use the same names
        //  Use command parameter type as it is unknown
        "args" -> OptionParameter(typeCheckingFunction, typeCheckingParameterName, InternalAggregators.theVarargAggregator, parameterName)
        else -> throwInternal("Unknown internal vararg parameter: $parameterName")
    }
}