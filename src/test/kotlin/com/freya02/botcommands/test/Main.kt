package com.freya02.botcommands.test

import com.freya02.botcommands.api.core.BBuilder
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.jdabuilder.light
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import kotlinx.coroutines.cancel
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger { }

fun main() {
    //stacktrace-decoroutinator seems to have issues when reloading with hotswap agent
    if ("-XX:HotswapAgent=fatjar" !in ManagementFactory.getRuntimeMXBean().inputArguments) {
        DecoroutinatorRuntime.load()
    } else {
        logger.info("Skipping stacktrace-decoroutinator as HotswapAgent is active")
    }

    val config = Config.readConfig()

    val scope = getDefaultScope()
    val manager = CoroutineEventManager(scope, 1.minutes)
    manager.listener<ShutdownEvent> {
        scope.cancel()
    }

    BBuilder.newBuilder({
        addSearchPath("com.freya02.botcommands.test.commands_kt")
        addSearchPath("com.freya02.botcommands.test.resolvers")
        addClass(TestDB::class.java)

        services {
            registerInstanceSupplier(Config::class.java) { config }
        }

        components {
            useComponents = true
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