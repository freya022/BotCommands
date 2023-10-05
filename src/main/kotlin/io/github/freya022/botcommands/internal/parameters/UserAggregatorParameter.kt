package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
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