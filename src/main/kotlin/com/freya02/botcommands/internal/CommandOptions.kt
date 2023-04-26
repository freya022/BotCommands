package com.freya02.botcommands.internal

import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.parameters.ParameterWrapper.Companion.wrap
import com.freya02.botcommands.internal.parameters.ResolverContainer
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import kotlin.reflect.KFunction

class CommandOptions internal constructor(
    methodParameters: List<AbstractOption>
) : ArrayList<AbstractOption>(methodParameters) {
    companion object {
        internal inline fun <reified T : OptionBuilder, reified R : Any> transform(
            context: BContextImpl,
            function: KFunction<*>,
            options: Map<String, OptionBuilder>,
            config: Configuration<T, R>
        ): CommandOptions {
            val resolverContainer = context.getService<ResolverContainer>()

            return CommandOptions(options.values.map { optionBuilder ->
                val kParameter = optionBuilder.parameter
                return@map when (optionBuilder) {
                    is T -> {
                        val parameter = when {
                            optionBuilder is SlashCommandOptionBuilder && optionBuilder.varArgs > 0 -> {
                                val elementsType = kParameter.collectionElementType
                                    ?: throwUser(kParameter.function, "List parameters must have a concrete element type")
                                kParameter.wrap().copy(type = elementsType)
                            }

                            else -> kParameter.wrap()
                        }

                        when (val resolver = resolverContainer.getResolver(parameter)) {
                            is R -> config.transformOption(optionBuilder, resolver)
                            else -> throwUser(
                                function,
                                "Expected a resolver of type ${R::class.simpleName!!} but ${resolver.javaClass.simpleName} does not support it"
                            )
                        }
                    }
                    is GeneratedOptionBuilder -> optionBuilder.toGeneratedMethodParameter(kParameter)
                    else -> throwInternal("Unsupported option builder: $optionBuilder")
                }
            })
        }
    }

    interface Configuration<T, R> {
        fun isOption(optionBuilder: T): Boolean = false

        fun transformOption(optionBuilder: T, resolver: R): AbstractOption =
            throwInternal("This should have not been called")
    }
}