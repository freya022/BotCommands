package io.github.freya022.botcommands.api.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver

interface MessageCommandBuilder : ApplicationCommandBuilder<MessageCommandOptionAggregateBuilder>,
                                  ITopLevelApplicationCommandBuilder {
    /**
     * Declares an input option, supported types and modifiers are in [ParameterResolver],
     * additional types can be added by implementing [MessageContextParameterResolver].
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     */
    fun option(declaredName: String)
}
