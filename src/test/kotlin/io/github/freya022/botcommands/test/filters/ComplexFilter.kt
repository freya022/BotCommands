package io.github.freya022.botcommands.test.filters

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandVariation
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@BService
object InVoiceChannel : ApplicationCommandFilter, TextCommandFilter, ComponentInteractionFilter {
    override val global: Boolean = false

    override suspend fun checkSuspend(
        event: MessageReceivedEvent,
        commandVariation: TextCommandVariation,
        args: String
    ): Any? = check(event.member)

    override suspend fun checkSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): Any? = check(event.member)

    override fun check(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?
    ): Any? = check(event.member)

    private fun check(member: Member?): String? {
        if (member?.voiceState?.inAudioChannel() == true) {
            return null
        }
        return "You must be in a voice channel"
    }
}

@BService
object IsBotOwner : ApplicationCommandFilter, TextCommandFilter, ComponentInteractionFilter {
    override val global: Boolean = false

    override suspend fun checkSuspend(
        event: MessageReceivedEvent,
        commandVariation: TextCommandVariation,
        args: String
    ): Any? = check(event.guild, event.author)

    override suspend fun checkSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): Any? = check(event.guild, event.user)

    override suspend fun checkSuspend(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?
    ): Any? = check(event.guild, event.user)

    private fun check(guild: Guild?, userSnowflake: UserSnowflake): String? {
        if (guild?.ownerIdLong != userSnowflake.idLong) {
            return "You must be the bot owner"
        }
        return null
    }
}

@BService
class IsGuildOwner(private val context: BContext) : ApplicationCommandFilter, TextCommandFilter, ComponentInteractionFilter {
    override val global: Boolean = false

    override suspend fun checkSuspend(
        event: MessageReceivedEvent,
        commandVariation: TextCommandVariation,
        args: String
    ): Any? = check(event.author)

    override suspend fun checkSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): Any? = check(event.user)

    override suspend fun checkSuspend(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?
    ): Any? = check(event.user)

    private fun check(userSnowflake: UserSnowflake): String? {
        if (!context.isOwner(userSnowflake.idLong)) {
            return "You must be the bot owner"
        }
        return null
    }
}