package com.freya02.botcommands.internal

import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.application.builder.findOption
import com.freya02.botcommands.api.parameters.CustomResolver
import com.freya02.botcommands.api.parameters.ParameterResolvers
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

internal fun interface MethodParameterSupplier<R : Any> {
    fun supply(kParameter: KParameter, name: String, resolver: R): MethodParameter
}

class MethodParameters private constructor(methodParameters: List<MethodParameter>)
    : ArrayList<MethodParameter>(methodParameters) {

    val optionCount: Int
        get() = this.count { it.isOption }

    companion object {
        internal inline fun <reified R : Any> of(
            function: KFunction<*>,
            optionBuilders: Map<String, OptionBuilder>,
            parameterSupplier: MethodParameterSupplier<R>
        ): MethodParameters {
            val methodParameters: MutableList<MethodParameter> = ArrayList(function.parameters.size)
            val kParameters = function.valueParameters.drop(1)

            for ((globalIndex, kParameter) in kParameters.withIndex()) {
                val paramType = kParameter.type
                val resolver = ParameterResolvers.of(ParameterType.ofType(paramType))

                requireUser(resolver != null, function) {
                    "Parameter #$globalIndex of type '${paramType.simpleName}' and name '${kParameter.bestName}' does not have any compatible resolver"
                }

                val parameterName = kParameter.findDeclarationName()
                val parameter = when (resolver) {
                    is R -> parameterSupplier.supply(kParameter, parameterName, resolver)
                    is CustomResolver -> CustomMethodParameter(
                        kParameter,
                        optionBuilders.findOption(parameterName),
                        resolver
                    )
                    else -> throwUser(
                        function,
                        "Expected a resolver of type ${R::class.simpleName!!} but ${resolver.javaClass.simpleName} does not support it"
                    )
                }

                methodParameters.add(parameter)
            }

            return MethodParameters(methodParameters)
        }
    }
}