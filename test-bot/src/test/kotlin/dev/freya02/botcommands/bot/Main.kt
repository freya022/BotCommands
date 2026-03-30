package dev.freya02.botcommands.bot

import ch.qos.logback.classic.ClassicConstants
import dev.freya02.botcommands.bot.config.Environment
import dev.freya02.botcommands.method.accessors.api.MethodAccessorsConfig
import dev.freya02.botcommands.method.accessors.api.annotations.ExperimentalMethodAccessorsApi
import dev.freya02.botcommands.restarter.api.BotCommandsRestarter
import dev.freya02.botcommands.restarter.api.annotations.ExperimentalRestartApi
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.DevConfig
import io.github.freya022.botcommands.api.core.config.registerComponents
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.DiscordLocale
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds

object Main {
    private val logger by lazy { KotlinLogging.logger { } }

    @JvmStatic
    fun main(args: Array<out String>) {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }

        @OptIn(ExperimentalRestartApi::class)
        BotCommandsRestarter.initialize(args)

        try {
            @OptIn(ExperimentalMethodAccessorsApi::class)
            MethodAccessorsConfig.preferClassFileAccessors()

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

                registerComponents()

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
