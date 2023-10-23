package io.github.freya022.botcommands.test.filters

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@BService
object InVoiceChannel : ApplicationCommandFilter, TextCommandFilter, ComponentInteractionFilter {
    override val global: Boolean = false

    override suspend fun isAcceptedSuspend(
        event: MessageReceivedEvent,
        commandVariation: TextCommandVariation,
        args: String
    ): Boolean {
        return event.member?.voiceState?.inAudioChannel() ?: false
    }

    override suspend fun isAcceptedSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): Boolean {
        val inVoice = event.member?.voiceState?.inAudioChannel() ?: false
        if (!inVoice) {
            event.reply_("You must be in a voice channel", ephemeral = true).await()
        }
        return inVoice
    }

    override suspend fun isAcceptedSuspend(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?
    ): Boolean {
        val inVoice = event.member?.voiceState?.inAudioChannel() ?: false
        if (!inVoice) {
            event.reply_("You must be in a voice channel", ephemeral = true).await()
        }
        return inVoice
    }
}

@BService
object IsBotOwner : ApplicationCommandFilter, TextCommandFilter, ComponentInteractionFilter {
    override val global: Boolean = false

    override suspend fun isAcceptedSuspend(
        event: MessageReceivedEvent,
        commandVariation: TextCommandVariation,
        args: String
    ): Boolean {
        if (!event.isFromGuild) return false
        return event.guild.ownerIdLong == event.author.idLong
    }

    override suspend fun isAcceptedSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): Boolean {
        val isBotOwner = event.guild?.ownerIdLong == event.user.idLong
        if (!isBotOwner) {
            event.reply_("You must be the bot owner", ephemeral = true).await()
        }
        return isBotOwner
    }

    override suspend fun isAcceptedSuspend(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?
    ): Boolean {
        val isBotOwner = event.guild?.ownerIdLong == event.user.idLong
        if (!isBotOwner) {
            event.reply_("You must be the bot owner", ephemeral = true).await()
        }
        return isBotOwner
    }
}

@BService
class IsGuildOwner(private val context: BContext) : ApplicationCommandFilter, TextCommandFilter, ComponentInteractionFilter {
    override val global: Boolean = false

    override suspend fun isAcceptedSuspend(
        event: MessageReceivedEvent,
        commandVariation: TextCommandVariation,
        args: String
    ): Boolean {
        return context.isOwner(event.author.idLong)
    }

    override suspend fun isAcceptedSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): Boolean {
        val isOwner = context.isOwner(event.user.idLong)
        if (!isOwner) {
            event.reply_("You must be the guild owner", ephemeral = true).await()
        }
        return isOwner
    }

    override suspend fun isAcceptedSuspend(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?
    ): Boolean {
        val isOwner = context.isOwner(event.user.idLong)
        if (!isOwner) {
            event.reply_("You must be the guild owner", ephemeral = true).await()
        }
        return isOwner
    }
}