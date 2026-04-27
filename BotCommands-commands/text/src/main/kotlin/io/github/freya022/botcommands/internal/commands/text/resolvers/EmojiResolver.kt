package io.github.freya022.botcommands.internal.commands.text.resolvers

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.api.utils.EmojiUtils
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

@Resolver
@ServiceName("textCommandEmojiResolver")
internal class EmojiResolver : ClassParameterResolver<EmojiResolver, Emoji>(Emoji::class),
                               TextParameterResolver<EmojiResolver, Emoji> {

    override val pattern: Pattern = Pattern.compile("(\\S+)")
    override val testExample: String = "<:name:1234>"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = ":joy:"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): Emoji? {
        val arg = args[0]!!
        val emoteMatcher = Message.MentionType.EMOJI.pattern.matcher(arg)
        return if (emoteMatcher.find()) {
            Emoji.fromCustom(
                emoteMatcher.group(1),
                emoteMatcher.group(2).toULong().toLong(),
                arg.startsWith("<a"),
            )
        } else {
            EmojiUtils.resolveJDAEmojiOrNull(arg)
        }
    }
}
