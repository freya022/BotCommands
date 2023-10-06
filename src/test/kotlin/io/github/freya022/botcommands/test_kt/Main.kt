package io.github.freya022.botcommands.test_kt

import ch.qos.logback.classic.ClassicConstants
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import io.github.freya022.botcommands.api.core.BBuilder
import io.github.freya022.botcommands.api.core.config.DevConfig
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.freya022.botcommands.test_kt.config.Environment
import kotlinx.coroutines.cancel
import mu.KotlinLogging
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.lang.management.ManagementFactory
import kotlin.io.path.absolutePathString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

object Main {
    private val logger by lazy { KotlinLogging.logger { } }

    @JvmStatic
    fun main(args: Array<out String>) {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        logger.info("Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}")

        // I use hotswap agent in order to update my code without restarting the bot
        // Of course this only supports modifying existing code
        // Refer to https://github.com/HotswapProjects/HotswapAgent#readme on how to use hotswap

        // stacktrace-decoroutinator has issues when reloading with hotswap agent
        when {
            "-XX:+AllowEnhancedClassRedefinition" in ManagementFactory.getRuntimeMXBean().inputArguments ->
                logger.info("Skipping stacktrace-decoroutinator as enhanced hotswap is active")

            "--no-decoroutinator" in args ->
                logger.info("Skipping stacktrace-decoroutinator as --no-decoroutinator is specified")

            else -> DecoroutinatorRuntime.load()
        }

        // Create a scope for our event manager
        val scope = namedDefaultScope("BC Test Coroutine", 4)
        val manager = CoroutineEventManager(scope, 1.minutes)
        manager.listener<ShutdownEvent> {
            scope.cancel()
        }

        BBuilder.newBuilder(manager) {
            disableExceptionsInDMs = true
            queryLogThreshold = 250.milliseconds

            addSearchPath("io.github.freya022.botcommands.test_kt")

            @OptIn(DevConfig::class)
            dumpLongTransactions = true

            components {
                useComponents = true
            }

            textCommands {
                usePingAsPrefix = true
            }

            applicationCommands {
                @OptIn(DevConfig::class)
                onlineAppCommandCheckEnabled = true
                addLocalizations("MyCommands", DiscordLocale.ENGLISH_US, DiscordLocale.ENGLISH_UK, DiscordLocale.FRENCH)
            }
        }
    }
}
