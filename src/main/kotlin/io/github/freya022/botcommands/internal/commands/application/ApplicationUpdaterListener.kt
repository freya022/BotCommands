package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import java.util.*

@BService
internal class ApplicationUpdaterListener(private val applicationCommandsBuilder: ApplicationCommandsBuilder) {
    private val logger = KotlinLogging.logger { }

    private val failedGuilds: MutableSet<Long> = Collections.synchronizedSet(hashSetOf())

    @BEventListener
    suspend fun onGuildAvailable(event: GuildAvailableEvent) {
        logger.trace("Trying to force update commands due to an unavailable guild becoming available")
        tryUpdate(event.guild, force = true)
    }

    @BEventListener
    suspend fun onGuildJoin(event: GuildJoinEvent) {
        logger.trace("Trying to force update commands due to a joined guild")
        tryUpdate(event.guild, force = true)
    }

    private suspend fun tryUpdate(guild: Guild, force: Boolean) {
        try {
            val hadFailed = failedGuilds.remove(guild.idLong)
            applicationCommandsBuilder.updateGuildCommands(guild, force = force || hadFailed)
        } catch (e: Throwable) {
            failedGuilds.add(guild.idLong)
            applicationCommandsBuilder.handleGuildCommandUpdateException(guild, e)
        }
    }
}