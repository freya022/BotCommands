package io.github.freya022.botcommands.internal.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal class UserCommandOptionAggregateBuilderImpl internal constructor(
    override val context: BContext,
    override val declarationSiteHolder: IDeclarationSiteHolder,
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
        UserCommandOptionAggregateBuilderImpl(context, declarationSiteHolder, aggregatorParameter, aggregator)
}