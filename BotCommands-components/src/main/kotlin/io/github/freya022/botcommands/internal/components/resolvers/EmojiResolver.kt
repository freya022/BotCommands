package io.github.freya022.botcommands.internal.components.resolvers

import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.components.serialization.SerializedComponentData
import io.github.freya022.botcommands.api.components.timeout.options.TimeoutOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.api.utils.EmojiUtils
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@Resolver
@ServiceName("componentEmojiResolver")
@RequiresComponents
internal class EmojiResolver : ClassParameterResolver<EmojiResolver, Emoji>(Emoji::class),
                               ComponentParameterResolver<EmojiResolver, Emoji>,
                               TimeoutParameterResolver<EmojiResolver, Emoji> {

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        data: SerializedComponentData,
    ): Emoji? = getEmoji(data.asString())

    override fun serialize(obj: Emoji) = SerializedComponentData.fromString(obj.formatted)


    override suspend fun resolveSuspend(option: TimeoutOption, data: SerializedComponentData): Emoji? =
        getEmoji(data.asString())


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
