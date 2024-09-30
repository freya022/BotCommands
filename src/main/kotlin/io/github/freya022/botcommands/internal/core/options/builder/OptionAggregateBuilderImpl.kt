package io.github.freya022.botcommands.internal.core.options.builder

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import io.github.freya022.botcommands.internal.core.service.canCreateWrappedService
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import io.github.freya022.botcommands.internal.utils.requireAt
import kotlin.reflect.KFunction

@CommandDSL
internal abstract class OptionAggregateBuilderImpl<T : OptionAggregateBuilder<T>> internal constructor(
    internal val aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>,
) : OptionAggregateBuilderContainerMixin<T>,
    OptionAggregateBuilder<T> {

    internal val aggregator: KFunction<*> = aggregator.reflectReference()
    internal val parameter = aggregatorParameter.typeCheckingParameter

    private val _optionBuilders: MutableMap<String, MutableList<OptionBuilderImpl>> = mutableMapOf()
    internal val optionBuilders: Map<String, List<OptionBuilderImpl>>
        get() = _optionBuilders

    private val aggregateContainer = OptionAggregateBuilderContainerMixinImpl(aggregator, ::constructNestedAggregate)
    final override val optionAggregateBuilders: Map<String, T> get() = aggregateContainer.optionAggregateBuilders

    init {
        //Do not check return type of trusted aggregators
        requireAt(
            aggregator.isSpecialAggregator() || aggregator.returnType == aggregatorParameter.typeCheckingParameter.type,
            aggregator
        ) {
            "Aggregator should have the same return type as the parameter (required: ${aggregatorParameter.typeCheckingParameter.type}, found: ${aggregator.returnType})"
        }
    }

    protected abstract val context: BContext
    protected abstract val declarationSiteHolder: IDeclarationSiteHolder

    final override fun hasVararg(): Boolean = aggregateContainer.hasVararg()

    final override fun serviceOption(declaredName: String) {
        this += ServiceOptionBuilderImpl(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    final override fun customOption(declaredName: String) {
        if (context.serviceContainer.canCreateWrappedService(aggregatorParameter.typeCheckingParameter) == null) {
            objectLogger().warn { "Using ${this::customOption.resolveBestReference().getSignature(source = false)} **for services** has been deprecated, please use ${this::serviceOption.resolveBestReference().getSignature(source = false)} instead, parameter '$declaredName' of ${declarationSiteHolder.declarationSite}" }
            return serviceOption(declaredName)
        }
        this += CustomOptionBuilderImpl(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    final override fun selfAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.selfAggregate(declaredName, block)

    final override fun varargAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.varargAggregate(declaredName, block)

    final override fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) =
        aggregateContainer.aggregate(declaredName, aggregator, block)

    internal abstract fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): T

    internal operator fun plusAssign(optionBuilder: OptionBuilderImpl) {
        _optionBuilders.computeIfAbsent(optionBuilder.optionParameter.typeCheckingParameterName) { arrayListOf() }.add(optionBuilder)
    }
}