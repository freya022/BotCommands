package io.github.freya022.botcommands.internal.options

import io.github.freya022.botcommands.api.core.reflect.ParameterWrapper
import io.github.freya022.botcommands.api.core.reflect.wrap
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.parameters.AggregatedParameter
import io.github.freya022.botcommands.api.parameters.ResolverData
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import io.github.freya022.botcommands.api.parameters.resolvers.IParameterResolver
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.builder.*
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isVarargAggregator
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.parameters.ResolverContainer
import io.github.freya022.botcommands.internal.parameters.ServiceMethodOption
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwInternal

internal object CommandOptions {
    internal inline fun <reified T : OptionBuilderImpl, reified R : IParameterResolver<R>, P : AggregatedParameter> transform(
        parent: P,
        resolverData: ResolverData?,
        aggregateBuilder: OptionAggregateBuilderImpl<*>,
        optionFinalizer: (parent: P, optionBuilder: T, resolver: R) -> OptionImpl
    ): List<OptionImpl> {
        val context = parent.context
        val aggregator = aggregateBuilder.aggregator
        val options = aggregateBuilder.optionBuilders
        val resolverContainer = context.getService<ResolverContainer>()

        val expectedOptions = aggregator.nonEventParameters.size - aggregateBuilder.optionAggregateBuilders.size
        requireAt(aggregator.isSpecialAggregator() || options.size == expectedOptions, aggregator) {
            "Aggregator should have the same number of options as there is options declared, $expectedOptions options were found in the aggregator but ${options.size} were declared, " +
                    "you may have forgotten to put the event as the first parameter"
        }

        return options.values.flatten().map { optionBuilder ->
            when (optionBuilder) {
                is T -> {
                    val parameter = optionBuilder.innerWrappedParameter

                    val resolver = resolverContainer.getResolverOfType<R>(ResolverRequest(parameter, resolverData))
                    optionFinalizer(parent, optionBuilder, resolver)
                }
                is AbstractGeneratedOptionBuilderImpl -> optionBuilder.toGeneratedOption(parent)
                is ServiceOptionBuilderImpl -> ServiceMethodOption(parent, optionBuilder.optionParameter, context.serviceContainer)
                is CustomOptionBuilderImpl -> {
                    val parameter = optionBuilder.innerWrappedParameter

                    val resolver = resolverContainer.getResolverOfType<ICustomResolver<*, *>>(ResolverRequest(parameter, resolverData))
                    CustomMethodOption(parent, optionBuilder.optionParameter, resolver)
                }
                else -> throwInternal("Unsupported option builder: $optionBuilder")
            }
        }
    }

    private val OptionBuilderImpl.innerWrappedParameter: ParameterWrapper
        get() = when {
            optionParameter.executableFunction.isVarargAggregator() -> parameter.wrap().toListElementType()
            else -> parameter.wrap()
        }
}