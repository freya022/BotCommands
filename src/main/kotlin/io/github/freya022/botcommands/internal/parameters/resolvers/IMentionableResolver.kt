package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.utils.ifNullThrowInternal
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern

@Resolver
internal object IMentionableResolver : ClassParameterResolver<IMentionableResolver, IMentionable>(IMentionable::class),
    TextParameterResolver<IMentionableResolver, IMentionable>,
    SlashParameterResolver<IMentionableResolver, IMentionable> {

    //region Text
    override val pattern: Pattern =
        enumSetOf(MentionType.CHANNEL, MentionType.USER, MentionType.ROLE, MentionType.EMOJI, MentionType.SLASH_COMMAND)
            .joinToString(separator = "|") { it.pattern.pattern() }
            .toPattern()
    override val testExample: String = "</name group sub:1234>"

    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String {
        return event.member.asMention
    }

    override suspend fun resolveSuspend(
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): IMentionable? {
        val filteredArgs = args.filterNotNull()
        // ID is always on the right side
        val id = filteredArgs
            .lastOrNull().ifNullThrowInternal { "Pattern matched but no args were present" }
            .toLongOrNull().ifNullThrowInternal { "ID matched but was not a Long" }
        return if (filteredArgs.size >= 2) {
            val name = filteredArgs[0]
            // Both emojis and slash commands can have two groups
            if (filteredArgs.size == 2) {
                val emoji = event.message.mentions.customEmojis.firstOrNull { it.idLong == id }
                if (emoji != null)
                    return emoji
            }

            // Can't use ID only as it's for top-level command
            event.message.mentions.slashCommands.firstOrNull {
                when (filteredArgs.size) {
                    2 -> id == it.idLong && name == it.name
                    3 -> id == it.idLong && name == it.name && filteredArgs[1] == it.subcommandName
                    4 -> id == it.idLong && name == it.name && filteredArgs[1] == it.subcommandGroup && filteredArgs[2] == it.subcommandName
                    else -> throwInternal("Matched more than 4 for slash commands")
                }
            }
        } else {
            event.message.mentions
                .getMentions(MentionType.CHANNEL, MentionType.USER, MentionType.ROLE)
                .firstOrNull { it.idLong == id }
        }
    }
    //endregion

    //region Slash
    override val optionType: OptionType = OptionType.MENTIONABLE

    override suspend fun resolveSuspend(
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): IMentionable = optionMapping.asMentionable
    //endregion
}