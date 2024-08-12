package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.builder.isGuildOnly
import io.github.freya022.botcommands.api.core.reflect.requireUser
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ResolverData
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import net.dv8tion.jda.api.interactions.InteractionContextType
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
        parameter.requireUser(InteractionContextType.GUILD in data.commandBuilder.topLevelBuilder.contexts) {
            "Commands that cannot run in guilds can't have a ${returnType.simpleNestedName} option"
        }

        if (parameter.isRequired) {
            parameter.requireUser(data.commandBuilder.topLevelBuilder.isGuildOnly) {
                "Commands executable outside of guilds cannot have a required ${returnType.simpleNestedName} option"
            }
        }
    }
}