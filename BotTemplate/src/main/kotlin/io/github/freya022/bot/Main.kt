package io.github.freya022.bot

import com.freya02.botcommands.api.core.BBuilder
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import io.github.freya022.bot.config.Config
import io.github.freya022.bot.config.Environment
import io.github.freya022.bot.utils.namedDefaultScope
import kotlinx.coroutines.cancel
import mu.KotlinLogging
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.lang.management.ManagementFactory
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes
import ch.qos.logback.classic.ClassicConstants as LogbackConstants

private val logger by lazy { KotlinLogging.logger {} } // Must not load before system property is set

object Main {
    @JvmStatic
    fun main(args: Array<out String>) {
        try {
            System.setProperty(LogbackConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
            logger.info("Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}")

            // Refer to https://github.com/HotswapProjects/HotswapAgent#readme on how to use hotswap
            // stacktrace-decoroutinator has issues when reloading with hotswap agent
            when {
                "-XX:+AllowEnhancedClassRedefinition" in ManagementFactory.getRuntimeMXBean().inputArguments ->
                    logger.info("Skipping stacktrace-decoroutinator as enhanced hotswap is active")

                "--no-decoroutinator" in args ->
                    logger.info("Skipping stacktrace-decoroutinator as --no-decoroutinator is specified")

                else -> DecoroutinatorRuntime.load()
            }

            val scope = namedDefaultScope("BotTemplate Coroutine", 4)
            val manager = CoroutineEventManager(scope, 1.minutes)
            manager.listener<ShutdownEvent> {
                scope.cancel()
            }

            val config = Config.instance

            BBuilder.newBuilder(manager) {
                if (Environment.isDev) {
                    disableExceptionsInDMs = true
                    disableAutocompleteCache = true
                }

                addOwners(*config.ownerIds.toLongArray())

                addSearchPath("io.github.freya022.bot")

                textCommands {
                    //Use ping as prefix if configured
                    usePingAsPrefix = "<ping>" in config.prefixes
                    prefixes += config.prefixes - "<ping>"
                }

                applicationCommands {
                    testGuildIds += config.testGuildIds

                    addLocalizations("Commands", DiscordLocale.FRENCH)
                }

                components {
                    useComponents = true
                }
            }

            logger.info("Loaded bot")
        } catch (e: Exception) {
            logger.error("Unable to start the bot", e)
            exitProcess(1)
        }
    }
}
