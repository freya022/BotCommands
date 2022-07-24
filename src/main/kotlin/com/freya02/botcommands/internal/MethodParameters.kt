package com.freya02.botcommands.internal

import com.freya02.botcommands.api.application.builder.CustomOptionBuilder
import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.parameters.ComponentParameterResolver
import com.freya02.botcommands.api.parameters.CustomResolver
import com.freya02.botcommands.commands.internal.ResolverContainer
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
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
        internal fun fakeOptionMap(context: BContextImpl, function: KFunction<*>): Map<String, OptionBuilder> =
            function.nonInstanceParameters.drop(1).associate {
                val name = it.findDeclarationName()

                when (context.serviceContainer.getService(ResolverContainer::class).getResolver(it)) {
                    is ComponentParameterResolver -> name to object : OptionBuilder(name) {}
                    is CustomResolver -> name to CustomOptionBuilder(name)
                    else -> TODO("Not implemented yet")
                }
            }

        internal inline fun <reified R : Any> of(
            context: BContextImpl,
            function: KFunction<*>,
            optionBuilders: Map<String, OptionBuilder>,
            parameterSupplier: InteractionParameterSupplier<R>
        ): MethodParameters {
            val methodParameters: MutableList<MethodParameter> = ArrayList(function.parameters.size)
            val kParameters = function.valueParameters.drop(1)

            val resolverContainer = context.serviceContainer.getService(ResolverContainer::class)
            for (kParameter in kParameters) {
                //TODO move parameter resolvers resolution in dedicated classes w/ transparent loading
                val resolver = resolverContainer.getResolver(kParameter)

                val parameterName = kParameter.findDeclarationName()
                val parameter = when (resolver) {
                    is R -> parameterSupplier.supply(kParameter, parameterName, resolver)
                    is CustomResolver -> CustomMethodParameter(
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