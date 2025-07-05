package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.springframework.stereotype.Component

// Stub resolver overridden by the built-in resolver
@Resolver(priority = -1)
@Component("overriddenUserResolver")
@ServiceName("overriddenUserResolver")
class UserResolver : ClassParameterResolver<UserResolver, User>(User::class), SlashParameterResolver<UserResolver, User> {
    override val optionType: OptionType = OptionType.USER
}