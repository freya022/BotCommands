package com.freya02.botcommands.internal

import com.freya02.botcommands.api.parameters.CustomResolver
import com.freya02.botcommands.api.parameters.ParameterResolvers
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.utils.StringUtils
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

internal fun interface MethodParameterSupplier<R : Any> {
    fun supply(globalIndex: Int, optionIndex: Int, kParameter: KParameter, resolver: R): MethodParameter
}

class MethodParameters private constructor(methodParameters: List<MethodParameter>)
    : ArrayList<MethodParameter>(methodParameters) {

    val optionCount: Int
        get() = this.count { it.isOption }

    companion object {
        internal inline fun <reified R : Any> of(
            function: KFunction<*>,
            optionAnnotations: List<KClass<out Annotation>>,
            parameterSupplier: MethodParameterSupplier<R>
        ): MethodParameters {
            val methodParameters: MutableList<MethodParameter> = ArrayList(function.parameters.size)
            val kParameters = function.valueParameters.drop(1)

            var optionIndex = 0
            for ((globalIndex, kParameter) in kParameters.withIndex()) {
                val paramType = kParameter.type
                val resolver = ParameterResolvers.of(ParameterType.ofType(paramType))

                requireUser(resolver != null, function) {
                    "Parameter #$globalIndex of type '${paramType.simpleName}' and name '${kParameter.bestName}' does not have any compatible resolver"
                }

                //Find if the parameter is an option
                if (kParameter.annotations.any { it.annotationClass in optionAnnotations }) {
                    optionIndex++

                    requireUser(resolver is R, function) {
                        "Expected a resolver of type ${R::class.simpleName!!} but ${resolver.javaClass.simpleName} does not support it"
                    }

                    methodParameters.add(parameterSupplier.supply(globalIndex, optionIndex, kParameter, resolver))
                } else { //Prob a custom parameter
                    if (resolver !is CustomResolver) {
                        throwUser(
                            function,
                            "Unsupported custom parameter: %s, did you forget to use %s on non-custom options ?".format(
                                paramType.jvmErasure.qualifiedName,
                                StringUtils.naturalJoin(
                                    "or",
                                    optionAnnotations.map { it.simpleName })
                            )
                        )
                    }

                    methodParameters.add(CustomMethodParameter(kParameter, resolver))
                }
            }

            return MethodParameters(methodParameters)
        }
    }
}