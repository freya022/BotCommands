package io.github.freya022.botcommands.internal.commands.application.resolvers.users

import io.github.freya022.botcommands.api.commands.application.context.user.options.UserContextCommandOption
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.entities.inputUser
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.users.AbstractInputUserResolver
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

@Resolver
internal class UserContextCommandInputUserResolver :
        AbstractInputUserResolver<UserContextCommandInputUserResolver>(),
        UserContextParameterResolver<UserContextCommandInputUserResolver, InputUser> {

    override fun resolve(option: UserContextCommandOption, event: UserContextInteractionEvent): InputUser {
        return event.inputUser
    }
}
