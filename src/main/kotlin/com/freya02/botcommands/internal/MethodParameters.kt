package com.freya02.botcommands.internal

import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.ResolverContainer
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

internal fun interface InteractionParameterSupplier<R : Any> {
    fun supply(kParameter: KParameter, name: String, resolver: R): MethodParameter
}

class MethodParameters internal constructor(methodParameters: List<MethodParameter>)
    : ArrayList<MethodParameter>(methodParameters) {

    val optionCount: Int
        get() = this.count { it.isOption }

    companion object {
        internal inline fun <reified R : Any> of(
            context: BContextImpl,
            function: KFunction<*>,
            parameterSupplier: InteractionParameterSupplier<R>
        ): MethodParameters {
            val methodParameters: MutableList<MethodParameter> = ArrayList(function.parameters.size)
            val kParameters = function.valueParameters.drop(1)

            val resolverContainer = context.getService(ResolverContainer::class)
            for (kParameter in kParameters) {
                //TODO move parameter resolvers resolution in dedicated classes w/ transparent loading
                val resolver = resolverContainer.getResolver(kParameter)

                val parameterName = kParameter.findDeclarationName()
                val parameter = when (resolver) {
                    is R -> parameterSupplier.supply(kParameter, parameterName, resolver)
                    is ICustomResolver<*, *> -> CustomMethodParameter(
                        kParameter,
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