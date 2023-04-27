package com.freya02.botcommands.internal

import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.api.commands.builder.GeneratedOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.api.parameters.ParameterWrapper.Companion.wrap
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.parameters.ResolverContainer
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters

object CommandOptions {
    internal inline fun <reified T : OptionBuilder, reified R : Any> transform(
        context: BContextImpl,
        aggregateBuilder: OptionAggregateBuilder,
        config: Configuration<T, R>
    ): List<AbstractOption> {
        val aggregator = aggregateBuilder.aggregator
        val options = aggregateBuilder.optionBuilders
        val resolverContainer = context.getService<ResolverContainer>()

        requireUser(options.size == aggregator.nonInstanceParameters.size - 1, aggregator) {
            "Aggregator should have the same number of parameters as there is options, found ${options.size} options and ${aggregator.nonInstanceParameters.size - 1} parameters, " +
                    "you may have forgotten to put the event as the first parameter"
        }

        return options.values.flatten().map { optionBuilder ->
            when (optionBuilder) {
                is T -> {
                    val kParameter = optionBuilder.parameter
                    val parameter = kParameter.wrap().toVarargElementType()

                    when (val resolver = resolverContainer.getResolver(parameter)) {
                        is R -> config.transformOption(optionBuilder, resolver)
                        else -> throwUser(
                            optionBuilder.owner,
                            "Expected a resolver of type ${R::class.simpleNestedName} but ${resolver.javaClass.simpleNestedName} does not support it"
                        )
                    }
                }
                is GeneratedOptionBuilder -> optionBuilder.toGeneratedMethodParameter()
                is CustomOptionBuilder -> {
                    val kParameter = optionBuilder.parameter
                    val parameter = kParameter.wrap().toVarargElementType()

                    when (val resolver = resolverContainer.getResolver(parameter)) {
                        is ICustomResolver<*, *> -> CustomMethodOption(kParameter, resolver)
                        else -> throwUser(
                            optionBuilder.owner,
                            "Expected a resolver of type ${ICustomResolver::class.simpleNestedName} but ${resolver.javaClass.simpleNestedName} does not support it"
                        )
                    }
                }
                else -> throwInternal("Unsupported option builder: $optionBuilder")
            }
        }
    }

    interface Configuration<T, R> {
        fun transformOption(optionBuilder: T, resolver: R): AbstractOption =
            throwInternal("This should have not been called")
    }
}