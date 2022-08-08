package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.commands.internal.application.ApplicationCommandsBuilder
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.api.annotations.BService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent
import java.util.*

@BService
internal class ApplicationUpdaterListener(private val applicationCommandsBuilder: ApplicationCommandsBuilder) {
    private val logger = Logging.getLogger()

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

    //Use this as a mean to detect OAuth scope changes
    @BEventListener
    suspend fun onGuildMemberUpdate(event: GuildMemberUpdateEvent) {
        if (event.member.idLong == event.jda.selfUser.idLong) {
            logger.trace("Trying to update commands due to a self member update")
            tryUpdate(event.guild, force = false)
        }
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