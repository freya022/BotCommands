package io.github.freya022.botcommands.internal.commands.application.context.message.options.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.context.message.options.builder.MessageCommandOptionAggregateBuilder
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal class MessageCommandOptionAggregateBuilderImpl internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilderImpl<MessageCommandOptionAggregateBuilder>(aggregatorParameter, aggregator),
    MessageCommandOptionAggregateBuilder {

    override fun option(declaredName: String) {
        this += MessageCommandOptionBuilderImpl(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilderImpl(
            aggregatorParameter.toOptionParameter(aggregator, declaredName),
            generatedValueSupplier
        )
    }

    override fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        MessageCommandOptionAggregateBuilderImpl(aggregatorParameter, aggregator)
}
