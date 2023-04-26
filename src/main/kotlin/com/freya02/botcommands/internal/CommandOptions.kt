package com.freya02.botcommands.internal

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.api.parameters.ParameterWrapper.Companion.wrap
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.parameters.ResolverContainer
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

class CommandOptions internal constructor(
    methodParameters: List<AbstractOption>
) : ArrayList<AbstractOption>(methodParameters) {
    companion object {
        internal inline fun <reified R : Any> transform(
            context: BContextImpl,
            function: KFunction<*>,
            options: Map<String, OptionBuilder>,
            config: Configuration<R>
        ): CommandOptions {
            val resolverContainer = context.getService<ResolverContainer>()

            return CommandOptions(function.valueParameters.drop(1).map { kParameter ->
                val optionBuilder = options[kParameter.findDeclarationName()]
                return@map when {
                    optionBuilder is GeneratedOptionBuilder -> optionBuilder.toGeneratedMethodParameter(kParameter)
                    config.isOption(kParameter) -> {
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
                            is R -> config.transformOption(kParameter, kParameter.findDeclarationName(), resolver)
                            else -> throwUser(
                                function,
                                "Expected a resolver of type ${R::class.simpleName!!} but ${resolver.javaClass.simpleName} does not support it"
                            )
                        }
                    }
                    config.isResolvable(kParameter) -> config.transformResolvable(kParameter)
                    else -> {
                        //TODO move parameter resolvers resolution in dedicated classes w/ transparent loading
                        when (val resolver = resolverContainer.getResolver(kParameter.wrap())) {
                            is R -> config.transformOption(kParameter, kParameter.findDeclarationName(), resolver)
                            is ICustomResolver<*, *> -> CustomMethodOption(kParameter, resolver)
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

    interface Configuration<R> {
        fun isOption(parameter: KParameter): Boolean = false
        fun isResolvable(parameter: KParameter): Boolean = false

        /**
         * This could be called if either [isOption] passes, or if none of them passes, as to try to pick the best resolver possible
         */
        fun transformOption(parameter: KParameter, declaredName: String, resolver: R): AbstractOption =
            throwInternal("This should have not been called")

        /**
         * Only called if [isResolvable] passes
         */
        fun transformResolvable(parameter: KParameter): AbstractOption =
            throwInternal("This should have not been called")
    }
}