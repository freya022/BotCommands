package dev.freya02.botcommands.bot

import ch.qos.logback.classic.ClassicConstants
import dev.freya02.botcommands.bot.config.Environment
import dev.freya02.botcommands.method.accessors.api.annotations.ExperimentalMethodAccessorsApi
import dev.reformator.stacktracedecoroutinator.jvm.DecoroutinatorJvmApi
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.DevConfig
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

            // I use hotswap agent to update my code without restarting the bot
            // Of course this only supports modifying existing code
            // Refer to https://github.com/HotswapProjects/HotswapAgent#readme on how to use hotswap

            // stacktrace-decoroutinator has issues when reloading with hotswap agent
            if ("-XX:+AllowEnhancedClassRedefinition" in ManagementFactory.getRuntimeMXBean().inputArguments) {
                logger.info { "Skipping stacktrace-decoroutinator as enhanced hotswap is active" }
            } else if ("--no-decoroutinator" in args) {
                logger.info { "Skipping stacktrace-decoroutinator as --no-decoroutinator is specified" }
            } else {
                DecoroutinatorJvmApi.install()
            }

            @OptIn(ExperimentalMethodAccessorsApi::class)
            BotCommands.preferClassFileAccessors()

            BotCommands.create {
                disableExceptionsInDMs = true

                addSearchPath("dev.freya02.botcommands.bot")

                database {
                    queryLogThreshold = 250.milliseconds

                    @OptIn(DevConfig::class)
                    dumpLongTransactions = true
                }

                localization {
                    responseBundles += "Test"
                }

                components {
                    enable = true
                }

                textCommands {
                    enable = true

                    usePingAsPrefix = true
                }

                services {
                    debug = false
                }

                applicationCommands {
                    enable = true

                    databaseCache {
                        @OptIn(DevConfig::class)
                        checkOnline = true
                    }

                    addLocalizations("MyCommands", DiscordLocale.ENGLISH_US, DiscordLocale.ENGLISH_UK, DiscordLocale.FRENCH)
                }

                modals {
                    enable = true
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Could not start the test bot" }
            exitProcess(1)
        }
    }
}
