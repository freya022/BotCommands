package io.github.freya022.botcommands.internal

import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.api.commands.builder.GeneratedOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.parameters.ResolverContainer
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isVarargAggregator
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import io.github.freya022.botcommands.internal.utils.requireUser
import io.github.freya022.botcommands.internal.utils.throwInternal

internal object CommandOptions {
    internal inline fun <reified T : OptionBuilder, reified R : IParameterResolver<R>> transform(
        context: BContext,
        aggregateBuilder: OptionAggregateBuilder<*>,
        config: Configuration<T, R>
    ): List<Option> {
        val aggregator = aggregateBuilder.aggregator
        val options = aggregateBuilder.optionBuilders
        val resolverContainer = context.getService<ResolverContainer>()

        val expectedOptions = aggregator.nonEventParameters.size - aggregateBuilder.nestedAggregates.size
        requireUser(aggregator.isSpecialAggregator() || options.size == expectedOptions, aggregator) {
            "Aggregator should have the same number of options as there is options declared, $expectedOptions options were found in the aggregator but ${options.size} were declared, " +
                    "you may have forgotten to put the event as the first parameter"
        }

        return options.values.flatten().map { optionBuilder ->
            when (optionBuilder) {
                is T -> {
                    val parameter = optionBuilder.innerWrappedParameter

                    val resolver = resolverContainer.getResolverOfType<R>(parameter)
                    config.transformOption(optionBuilder, resolver)
                }
                is GeneratedOptionBuilder -> optionBuilder.toGeneratedOption()
                is CustomOptionBuilder -> {
                    val parameter = optionBuilder.innerWrappedParameter

                    val resolver = resolverContainer.getResolverOfType<ICustomResolver<*, *>>(parameter)
                    CustomMethodOption(optionBuilder.optionParameter, resolver)
                }
                else -> throwInternal("Unsupported option builder: $optionBuilder")
            }
        }
    }

    interface Configuration<T, R> {
        fun transformOption(optionBuilder: T, resolver: R): Option =
            throwInternal("This should have not been called")
    }

    private val OptionBuilder.innerWrappedParameter: ParameterWrapper
        get() = when {
            optionParameter.executableFunction.isVarargAggregator() -> parameter.wrap().toListElementType()
            else -> parameter.wrap()
        }
}