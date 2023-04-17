package com.freya02.botcommands.test_kt

import com.freya02.botcommands.api.core.BBuilder
import com.freya02.botcommands.test.BasicSettingsProvider
import com.freya02.botcommands.test.Config
import com.freya02.botcommands.test.TestDB
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import mu.KotlinLogging
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.minutes

object Main {
    private val logger = KotlinLogging.logger { }

    @JvmStatic
    fun main(args: Array<out String>) {
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
            disableExceptionsInDMs = true

            addSearchPath("com.freya02.botcommands.test_kt")

            //Still kept in the java test package
            addClass(Config::class.java)
            addClass(TestDB::class.java)
            addClass(BasicSettingsProvider::class.java)

            components {
                useComponents = true
            }

            textCommands {
                usePingAsPrefix = true
            }

            applicationCommands {
                onlineAppCommandCheckEnabled = true
                addLocalizations("MyCommands", DiscordLocale.ENGLISH_US, DiscordLocale.ENGLISH_UK, DiscordLocale.FRENCH)
            }
        }, manager)
    }
}
