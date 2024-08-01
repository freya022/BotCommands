package io.github.freya022.botcommands.api.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver

interface UserCommandOptionAggregateBuilder : ApplicationCommandOptionAggregateBuilder<UserCommandOptionAggregateBuilder> {
    /**
     * Declares an input option, supported types and modifiers are in [ParameterResolver],
     * additional types can be added by implementing [UserContextParameterResolver].
     *
     * @param declaredName Name of the declared parameter in the aggregator
     */
    fun option(declaredName: String)
}
