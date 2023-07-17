package com.freya02.botcommands.api.core.events

import net.dv8tion.jda.api.JDA

class InjectedJDAEvent internal constructor(val jda: JDA) : BEvent()