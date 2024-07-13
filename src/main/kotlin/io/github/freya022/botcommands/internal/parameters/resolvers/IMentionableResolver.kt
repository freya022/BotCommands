package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandOption
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
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
        // The pattern being a combination of N patterns with each their set of groups, most groups will be null/empty.
        // The last element is used as this is correct for channel/user/role/emoji/slash commands
        val groups = args.filterNotNull()
        val id = groups.last().toLongOrNull()
            ?: throwInternal("Unable to parse the ID of a mentionable, pattern should have rejected the command")
        return if (groups.size == 4) {
            // For now, only slash commands have that many groups
            // Slash commands need special handling as a command ID refers to the top level command,
            // so we need to differentiate subcommands (and groups)
            event.message.mentions.slashCommands.first {
                it.idLong == id &&
                        groups[0] == it.name &&
                        groups[1] == it.subcommandGroup &&
                        groups[2] == it.subcommandName
            }
        } else {
            event.message.mentions.getMentions().first { it.idLong == id }
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