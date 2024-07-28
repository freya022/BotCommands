package io.github.freya022.botcommands.api.core.options.builder

import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.commands.builder.ServiceOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixin
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixinImpl
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import io.github.freya022.botcommands.internal.utils.requireAt
import kotlin.reflect.KFunction

@CommandDSL
abstract class OptionAggregateBuilder<T : OptionAggregateBuilder<T>> internal constructor(
    internal val aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : OptionAggregateBuilderContainerMixin<T>,
    OptionRegistry<T> {

    internal val aggregator: KFunction<*> = aggregator.reflectReference()
    internal val parameter = aggregatorParameter.typeCheckingParameter

    private val _optionBuilders: MutableMap<String, MutableList<OptionBuilder>> = mutableMapOf()
    internal val optionBuilders: Map<String, List<OptionBuilder>>
        get() = _optionBuilders

    private val aggregateContainer = OptionAggregateBuilderContainerMixinImpl(aggregator, ::constructNestedAggregate)
    override val optionAggregateBuilders: Map<String, T> get() = aggregateContainer.optionAggregateBuilders

    init {
        //Do not check return type of trusted aggregators
        requireAt(aggregator.isSpecialAggregator() || aggregator.returnType == aggregatorParameter.typeCheckingParameter.type, aggregator) {
            "Aggregator should have the same return type as the parameter (required: ${aggregatorParameter.typeCheckingParameter.type}, found: ${aggregator.returnType})"
        }
    }

    protected abstract val context: BContext
    protected abstract val declarationSiteHolder: IDeclarationSiteHolder

    override fun hasVararg(): Boolean = aggregateContainer.hasVararg()

    override fun serviceOption(declaredName: String) {
        this += ServiceOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun customOption(declaredName: String) {
        if (context.serviceContainer.canCreateWrappedService(aggregatorParameter.typeCheckingParameter) == null) {
            objectLogger().warn { "Using ${this::customOption.resolveBestReference().getSignature(source = false)} **for services** has been deprecated, please use ${this::serviceOption.resolveBestReference().getSignature(source = false)} instead, parameter '$declaredName' of ${declarationSiteHolder.declarationSite}" }
            return serviceOption(declaredName)
        }
        this += CustomOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun selfAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.selfAggregate(declaredName, block)

    override fun varargAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.varargAggregate(declaredName, block)

    override fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) =
        aggregateContainer.aggregate(declaredName, aggregator, block)

    internal abstract fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): T

    internal operator fun plusAssign(optionBuilder: OptionBuilder) {
        _optionBuilders.computeIfAbsent(optionBuilder.optionParameter.typeCheckingParameterName) { arrayListOf() }.add(optionBuilder)
    }
}
