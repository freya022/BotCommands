package io.github.freya022.botcommands.internal.parameters.resolvers.users

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

@Resolver
internal class TextCommandUserResolver(
    private val resolver: TextCommandInputUserResolver,
) : ClassParameterResolver<TextCommandUserResolver, User>(User::class),
    TextParameterResolver<TextCommandUserResolver, User> {

    override val pattern: Pattern get() = resolver.pattern
    override val testExample: String get() = resolver.testExample

    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String {
        return resolver.getHelpExample(option, event)
    }

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): User? {
        return resolver.resolveSuspend(option, event, args)
    }
}
