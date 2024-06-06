package io.github.freya022.botcommands.test

import ch.qos.logback.classic.ClassicConstants
import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.DevConfig
import io.github.freya022.botcommands.test.config.Environment
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.lang.management.ManagementFactory
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds

const val botName = "BC Test"

object Main {
    private val logger by lazy { KotlinLogging.logger { } }

    @JvmStatic
    fun main(args: Array<out String>) {
        try {
            System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
            logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }

            // I use hotswap agent in order to update my code without restarting the bot
            // Of course this only supports modifying existing code
            // Refer to https://github.com/HotswapProjects/HotswapAgent#readme on how to use hotswap

            // stacktrace-decoroutinator has issues when reloading with hotswap agent
            if ("-XX:+AllowEnhancedClassRedefinition" in ManagementFactory.getRuntimeMXBean().inputArguments) {
                logger.info { "Skipping stacktrace-decoroutinator as enhanced hotswap is active" }
            } else if ("--no-decoroutinator" in args) {
                logger.info { "Skipping stacktrace-decoroutinator as --no-decoroutinator is specified" }
            } else {
                DecoroutinatorRuntime.load()
            }

            BotCommands.create {
                disableExceptionsInDMs = true

                addSearchPath("io.github.freya022.botcommands.test")
                addSearchPath("doc")

                database {
                    queryLogThreshold = 250.milliseconds

                    @OptIn(DevConfig::class)
                    dumpLongTransactions = true
                }

                localization {
                    responseBundles += "Test"
                }

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
        } catch (e: Exception) {
            logger.error(e) { "Could not start the test bot" }
            exitProcess(1)
        }
    }
}
