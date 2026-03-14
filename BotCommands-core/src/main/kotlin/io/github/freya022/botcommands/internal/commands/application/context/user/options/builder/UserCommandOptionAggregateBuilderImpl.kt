package io.github.freya022.botcommands.internal.commands.application.context.user.options.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.context.user.options.builder.UserCommandOptionAggregateBuilder
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal class UserCommandOptionAggregateBuilderImpl internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilderImpl<UserCommandOptionAggregateBuilder>(aggregatorParameter, aggregator),
    UserCommandOptionAggregateBuilder {

    override fun option(declaredName: String) {
        this += UserCommandOptionBuilderImpl(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilderImpl(
            aggregatorParameter.toOptionParameter(aggregator, declaredName),
            generatedValueSupplier
        )
    }

    override fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        UserCommandOptionAggregateBuilderImpl(aggregatorParameter, aggregator)
}
