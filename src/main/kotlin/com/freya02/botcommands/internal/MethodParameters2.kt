package com.freya02.botcommands.internal

import com.freya02.botcommands.api.parameters.CustomResolver
import com.freya02.botcommands.commands.internal.ResolverContainer
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

class MethodParameters2 {
    class Configuration<R> {
        var optionPredicate: (KParameter) -> Boolean = { false }
        var resolvablePredicate: (KParameter) -> Boolean = { false }

        /**
         * This could be called if either the [optionPredicate] passes, or if none of them passes, as to try to pick the best resolver possible
         */
        lateinit var optionTransformer: (KParameter, String, R) -> MethodParameter

        /**
         * Only called if the [resolvablePredicate] passes
         */
        lateinit var resolvableTransformer: (KParameter) -> MethodParameter
    }

    companion object {
        internal inline fun <reified R : Any> transform(
            context: BContextImpl,
            function: KFunction<*>,
            configurationBlock: Configuration<R>.() -> Unit
        ): MethodParameters {
            val resolverContainer = context.serviceContainer.getService(ResolverContainer::class)
            val config = Configuration<R>().apply(configurationBlock)

            return MethodParameters(function.valueParameters.drop(1).map { kParameter ->
                return@map when {
                    config.optionPredicate(kParameter) -> {
                        //TODO move parameter resolvers resolution in dedicated classes w/ transparent loading
                        when (val resolver = resolverContainer.getResolver(kParameter)) {
                            is R -> config.optionTransformer(kParameter, kParameter.findDeclarationName(), resolver)
                            else -> throwUser(
                                function,
                                "Expected a resolver of type ${R::class.simpleName!!} but ${resolver.javaClass.simpleName} does not support it"
                            )
                        }
                    }
                    config.resolvablePredicate(kParameter) -> config.resolvableTransformer(kParameter)
                    else -> {
                        //TODO move parameter resolvers resolution in dedicated classes w/ transparent loading
                        when (val resolver = resolverContainer.getResolver(kParameter)) {
                            is R -> config.optionTransformer(kParameter, kParameter.findDeclarationName(), resolver)
                            is CustomResolver -> CustomMethodParameter(kParameter, resolver)
                            else -> throwUser(
                                function,
                                "Expected a resolver of type ${R::class.simpleName!!} but ${resolver.javaClass.simpleName} does not support it"
                            )
                        }
                    }
                }
            })
        }
    }
}
