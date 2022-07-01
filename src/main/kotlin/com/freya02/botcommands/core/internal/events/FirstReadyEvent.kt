package com.freya02.botcommands.core.internal.events

import com.freya02.botcommands.core.api.events.BEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent

class FirstReadyEvent internal constructor(private val event: GuildReadyEvent) : BEvent()