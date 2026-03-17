package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.annotations.BEventListener.RunMode
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent

private val logger = KotlinLogging.logger { }

@BService
@RequiresApplicationCommands
internal class ApplicationUpdaterListener(private val applicationCommandsBuilder: ApplicationCommandsBuilder) {

    @BEventListener(mode = RunMode.ASYNC)
    suspend fun onGuildAvailable(event: GuildAvailableEvent) {
        logger.trace { "Trying to force update commands due to an unavailable guild becoming available" }
        tryUpdate(event.guild)
    }

    @BEventListener(mode = RunMode.ASYNC)
    suspend fun onGuildJoin(event: GuildJoinEvent) {
        logger.trace { "Trying to force update commands due to a joined guild" }
        tryUpdate(event.guild)
    }

    private suspend fun tryUpdate(guild: Guild) {
        try {
            applicationCommandsBuilder.updateGuildCommands(guild, force = true)
        } catch (e: Throwable) {
            applicationCommandsBuilder.handleGuildCommandUpdateException(guild, e)
        }
    }
}
