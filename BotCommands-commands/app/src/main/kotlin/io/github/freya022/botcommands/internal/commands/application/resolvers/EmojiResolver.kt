package io.github.freya022.botcommands.internal.commands.application.resolvers

import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.utils.EmojiUtils
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
internal class EmojiResolver : ClassParameterResolver<EmojiResolver, Emoji>(Emoji::class),
                               SlashParameterResolver<EmojiResolver, Emoji> {

    override val optionType: OptionType get() = OptionType.STRING

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): Emoji? = getEmoji(optionMapping.asString)


    private fun getEmoji(arg: String): Emoji? {
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
