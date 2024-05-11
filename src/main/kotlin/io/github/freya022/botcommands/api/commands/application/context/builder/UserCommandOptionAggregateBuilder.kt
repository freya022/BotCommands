package io.github.freya022.botcommands.api.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import kotlin.reflect.KFunction

class UserCommandOptionAggregateBuilder internal constructor(
    private val context: BContext,
    private val commandBuilder: UserCommandBuilder,
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilder<UserCommandOptionAggregateBuilder>(aggregatorParameter, aggregator) {
    fun option(declaredName: String) {
        this += UserCommandOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun customOption(declaredName: String) {
        if (context.serviceContainer.canCreateWrappedService(aggregatorParameter.typeCheckingParameter) == null) {
            objectLogger().warn { "Using ${this::customOption.resolveBestReference().getSignature(source = false)} **for services** has been deprecated, please use ${this::serviceOption.resolveBestReference().getSignature(source = false)} instead, parameter '$declaredName' of ${commandBuilder.declarationSite}" }
            return serviceOption(declaredName)
        }
        this += CustomOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), generatedValueSupplier)
    }

    override fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        UserCommandOptionAggregateBuilder(context, commandBuilder, aggregatorParameter, aggregator)
}