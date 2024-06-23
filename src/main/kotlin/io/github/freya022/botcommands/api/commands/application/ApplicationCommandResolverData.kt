package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.core.reflect.throwUser
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ResolverData
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import kotlin.reflect.KClass

/**
 * Resolver data for application commands, passed when requesting:
 * - [SlashParameterResolver]
 * - [UserContextParameterResolver]
 * - [MessageContextParameterResolver]
 */
class ApplicationCommandResolverData internal constructor(
    val commandBuilder: ApplicationCommandBuilder<*>
) : ResolverData

internal fun ResolverRequest.checkGuildOnly(returnType: KClass<*>) {
    (resolverData as? ApplicationCommandResolverData)?.let { data ->
        //TODO[User apps] throw if there is no guild scope at all, regardless of nullability
        if (!data.commandBuilder.topLevelBuilder.scope.isGuildOnly && parameter.isRequired) {
            parameter.throwUser("Cannot get a required ${returnType.simpleNestedName} in a global command")
        }
    }
}