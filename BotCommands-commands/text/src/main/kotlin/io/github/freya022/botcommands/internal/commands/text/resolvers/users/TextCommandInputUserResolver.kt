package io.github.freya022.botcommands.internal.commands.text.resolvers.users

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.parameters.resolvers.users.AbstractInputUserResolver
import io.github.freya022.botcommands.internal.utils.ifNullThrowInternal
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

@Resolver
@RequiresTextCommands
internal class TextCommandInputUserResolver :
        AbstractInputUserResolver<TextCommandInputUserResolver>(),
        TextParameterResolver<TextCommandInputUserResolver, InputUser> {

    override val pattern: Pattern get() = userMentionPattern
    override val testExample: String = "<@1234>"

    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String {
        return event.member.asMention
    }

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): InputUser? {
        val id = args.filterNotNull()
            .singleOrNull().ifNullThrowInternal { "Pattern matched but no args were present" }
            .toLongOrNull().ifNullThrowInternal { "ID matched but was not a Long" }
        return retrieveOrNull(id, event.message)
    }

    internal companion object {
        internal val userMentionPattern: Pattern = Pattern.compile("<@(\\d+)>|(\\d+)")
    }
}
