package com.freya02.botcommands.api.core.events

import net.dv8tion.jda.api.events.guild.GuildReadyEvent

class FirstGuildReadyEvent internal constructor(val event: GuildReadyEvent) : BEvent()