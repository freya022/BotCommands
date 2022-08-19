package com.freya02.botcommands.internal.core.events

import com.freya02.botcommands.api.core.events.BEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent

class FirstReadyEvent internal constructor(private val event: GuildReadyEvent) : BEvent()