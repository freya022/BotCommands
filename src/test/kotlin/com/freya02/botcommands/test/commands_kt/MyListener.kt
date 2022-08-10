package com.freya02.botcommands.test.commands_kt

import com.freya02.botcommands.core.api.annotations.BEventListener
import net.dv8tion.jda.api.events.ReadyEvent

class MyListener {
    @BEventListener
    suspend fun readyListener(event: ReadyEvent) {
        println(event)
    }
}