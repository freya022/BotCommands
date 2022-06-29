package com.freya02.botcommands.commands.internal.application

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.core.api.annotations.BEventListener
import com.freya02.botcommands.core.internal.ClassPathContainer
import com.freya02.botcommands.core.internal.annotations.BInternalClass
import net.dv8tion.jda.api.events.guild.GuildReadyEvent

private val LOGGER = Logging.getLogger()

@BInternalClass
class ApplicationCommandsBuilder {
    @BEventListener
    internal fun onGuildReady(event: GuildReadyEvent, classPathContainer: ClassPathContainer) {
        LOGGER.debug("Guild ready: ${event.guild}")
    }
}