package com.freya02.botcommands.internal

import com.freya02.botcommands.api.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.ResolverContainer
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

class MethodParameters internal constructor(
    methodParameters: List<MethodParameter>
) : ArrayList<MethodParameter>(methodParameters) {
    val optionCount: Int
        get() = this.count { it.isOption }

    companion object {
        internal inline fun <reified R : Any> transform(
            context: BContextImpl,
            function: KFunction<*>,
            options: Map<String, OptionBuilder> = mapOf(),
            configurationBlock: Configuration<R>.() -> Unit
        ): MethodParameters {
            val resolverContainer = context.getService(ResolverContainer::class)
            val config = Configuration<R>().apply(configurationBlock)

            return MethodParameters(function.valueParameters.drop(1).map { kParameter ->
                return@map when {
                    options[kParameter.findDeclarationName()] is GeneratedOptionBuilder -> (options[kParameter.findDeclarationName()] as GeneratedOptionBuilder).toGeneratedMethodParameter(kParameter)
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
                            is ICustomResolver<*, *> -> CustomMethodParameter(kParameter, resolver)
                            else -> throwUser(
                                function,
                                "Expected a resolver of type ${R::class.simpleName!!} but ${resolver.javaClass.simpleName} does not support it"
                            )
                        }
                    }
                }
            })
        }

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
    }
}