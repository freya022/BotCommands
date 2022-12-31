package com.freya02.botcommands.test

import com.freya02.botcommands.api.core.BBuilder
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import mu.KotlinLogging
import net.dv8tion.jda.api.events.session.ShutdownEvent
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

    val scope = getDefaultScope()
    val manager = CoroutineEventManager(scope, 1.minutes)
    manager.listener<ShutdownEvent> {
        this.cancel() //"this" is a scope delegate
    }

    BBuilder.newBuilder({
        addSearchPath("com.freya02.botcommands.test.commands_kt")
        addSearchPath("com.freya02.botcommands.test.resolvers")
        addClass(JDAService::class.java)
        addClass(Config::class.java)
        addClass(TestDB::class.java)

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
}