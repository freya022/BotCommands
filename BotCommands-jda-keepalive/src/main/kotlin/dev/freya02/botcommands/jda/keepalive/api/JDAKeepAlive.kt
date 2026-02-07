package dev.freya02.botcommands.jda.keepalive.api

import dev.freya02.botcommands.jda.keepalive.internal.Agent

@ExperimentalKeepAliveApi
object JDAKeepAlive {

    @JvmStatic
    fun install() {
        Agent.load()
    }
}
