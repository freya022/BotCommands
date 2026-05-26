package io.github.freya022.botcommands.internal.commands.application.resolvers.users

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.context.user.options.UserContextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

@Resolver
@RequiresApplicationCommands
internal class UserContextCommandUserResolver(
    private val resolver: UserContextCommandInputUserResolver,
) : ClassParameterResolver<UserContextCommandUserResolver, User>(User::class),
    UserContextParameterResolver<UserContextCommandUserResolver, User> {

    override fun resolve(option: UserContextCommandOption, event: UserContextInteractionEvent): User {
        return resolver.resolve(option, event)
    }
}
