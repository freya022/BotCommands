package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.core.reflect.throwUser
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ResolverData
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import kotlin.reflect.KClass

internal class ApplicationCommandResolverData(
    val commandInfo: ApplicationCommandInfo
) : ResolverData

internal fun ResolverRequest.checkGuildOnly(returnType: KClass<*>) {
    (resolverData as? ApplicationCommandResolverData)?.let { data ->
        //TODO[User apps] throw if there is no guild scope at all, regardless of nullability
        if (!data.commandInfo.builder.topLevelBuilder.scope.isGuildOnly && parameter.isRequired) {
            parameter.throwUser("Cannot get a required ${returnType.simpleNestedName} in a global command")
        }
    }
}