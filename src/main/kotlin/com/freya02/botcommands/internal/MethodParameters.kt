package com.freya02.botcommands.internal

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.api.parameters.ParameterWrapper.Companion.wrap
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.parameters.ResolverContainer
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
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
            options: Map<String, CommandOptionBuilder> = mapOf(),
            configurationBlock: Configuration<R>.() -> Unit
        ): MethodParameters {
            val resolverContainer = context.getService<ResolverContainer>()
            val config = Configuration<R>().apply(configurationBlock)

            return MethodParameters(function.valueParameters.drop(1).map { kParameter ->
                val optionBuilder = options[kParameter.findDeclarationName()]
                return@map when {
                    optionBuilder is GeneratedOptionBuilder -> optionBuilder.toGeneratedMethodParameter(kParameter)
                    config.optionPredicate(kParameter) -> {
                        val parameter = when {
                            optionBuilder is SlashCommandOptionBuilder && optionBuilder.varArgs > 0 -> {
                                val elementsType = kParameter.collectionElementType
                                    ?: throwUser(kParameter.function, "List parameters must have a concrete element type")
                                kParameter.wrap().copy(type = elementsType)
                            }
                            else -> kParameter.wrap()
                        }

                        //TODO move parameter resolvers resolution in dedicated classes w/ transparent loading
                        when (val resolver = resolverContainer.getResolver(parameter)) {
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
                        when (val resolver = resolverContainer.getResolver(kParameter.wrap())) {
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