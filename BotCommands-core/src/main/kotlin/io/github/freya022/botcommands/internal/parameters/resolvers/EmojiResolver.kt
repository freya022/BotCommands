package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.api.utils.EmojiUtils
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern

@Resolver
class EmojiResolver : ClassParameterResolver<EmojiResolver, Emoji>(Emoji::class),
                      TextParameterResolver<EmojiResolver, Emoji>,
                      SlashParameterResolver<EmojiResolver, Emoji> {

    override val pattern: Pattern = Pattern.compile("(\\S+)")
    override val testExample: String = "<:name:1234>"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = ":joy:"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): Emoji? = getEmoji(args[0]!!)


    override val optionType: OptionType get() = OptionType.STRING

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Emoji? = getEmoji(optionMapping.asString)


    private fun getEmoji(arg: String): Emoji? {
        val emoteMatcher = Message.MentionType.EMOJI.pattern.matcher(arg)
        return if (emoteMatcher.find()) {
            Emoji.fromCustom(
                emoteMatcher.group(1),
                emoteMatcher.group(2).toULong().toLong(),
                arg.startsWith("<a")
            )
        } else {
            EmojiUtils.resolveJDAEmojiOrNull(arg)
        }
    }
}
