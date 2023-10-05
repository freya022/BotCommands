package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
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