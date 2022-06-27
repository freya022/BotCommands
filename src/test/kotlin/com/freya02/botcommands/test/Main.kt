package com.freya02.botcommands.test

import com.freya02.botcommands.core.api.BBuilder
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.cancel
import net.dv8tion.jda.api.events.ShutdownEvent
import kotlin.time.Duration.Companion.minutes

fun main() {
    val config = Config.readConfig()

    val scope = getDefaultScope()
    val manager = CoroutineEventManager(scope, 1.minutes)
    manager.listener<ShutdownEvent> {
        scope.cancel()
    }

    BBuilder.newBuilder(manager) {
        addSearchPath("com.freya02.botcommands.test.commands2")
    }

    println()

//    light(config.token, enableCoroutines = false) {
//        enableIntents(GatewayIntent.GUILD_MEMBERS)
//        setActivity(Activity.playing("coroutines go brrr"))
//        setEventManager(manager)
//    }.awaitReady()
}