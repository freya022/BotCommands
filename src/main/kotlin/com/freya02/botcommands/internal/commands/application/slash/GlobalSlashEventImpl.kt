package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.internal.BContextImpl
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

internal open class GlobalSlashEventImpl(
    private val context: BContextImpl,
    event: SlashCommandInteractionEvent
) : GlobalSlashEvent(event.jda, event.responseNumber, event.interaction) {
    override fun getContext(): BContext {
        return context
    }
}