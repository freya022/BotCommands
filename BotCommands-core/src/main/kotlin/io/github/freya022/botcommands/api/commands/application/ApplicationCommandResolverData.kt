package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.parameters.ResolverData
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver

/**
 * Resolver data for application commands, passed when requesting:
 * - [SlashParameterResolver]
 * - [UserContextParameterResolver]
 * - [MessageContextParameterResolver]
 */
class ApplicationCommandResolverData internal constructor(
    val commandBuilder: ApplicationCommandBuilder<*>
) : ResolverData
