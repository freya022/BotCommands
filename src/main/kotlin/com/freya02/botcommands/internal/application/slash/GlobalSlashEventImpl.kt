package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent
import com.freya02.botcommands.internal.BContextImpl
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import kotlin.reflect.KFunction

open class GlobalSlashEventImpl(
    private val context: BContextImpl,
    function: KFunction<*>,
    event: SlashCommandInteractionEvent
) : GlobalSlashEvent(context, function, event.jda, event.responseNumber, event.interaction) {
    override fun getContext(): BContext {
        return context
    }
}