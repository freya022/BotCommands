package com.freya02.botcommands.test

import com.freya02.botcommands.api.components.DefaultComponentManager
import com.freya02.botcommands.core.api.BBuilder
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.jdabuilder.light
import kotlinx.coroutines.cancel
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.time.Duration.Companion.minutes

fun main() {
    val config = Config.readConfig()
    val testDB = TestDB(config.dbConfig)

    val scope = getDefaultScope()
    val manager = CoroutineEventManager(scope, 1.minutes)
    manager.listener<ShutdownEvent> {
        scope.cancel()
    }

    BBuilder.newBuilder({
        addSearchPath("com.freya02.botcommands.test.commands_kt")
        addSearchPath("com.freya02.botcommands.test.resolvers")

        connectionProvider = testDB.connectionSupplier

        components {
            componentManagerStrategy = DefaultComponentManager::class.java
        }

        textCommands {
            usePingAsPrefix = true
        }

        applicationCommands {
            onlineAppCommandCheckEnabled = true
        }
    }, manager)

    light(config.token, enableCoroutines = false) {
        enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
        setActivity(Activity.playing("coroutines go brrr"))
        setEventManager(manager)
    }
}