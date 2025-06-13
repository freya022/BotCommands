package io.github.freya022.pokedex.bot

import ch.qos.logback.classic.ClassicConstants
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.pokedex.bot.config.Environment
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.path.absolutePathString

object Main {
    private val logger by lazy { KotlinLogging.logger { } }

    @JvmStatic
    fun main(args: Array<out String>) {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }

        BotCommands.create {
            disableExceptionsInDMs = true

            addSearchPath("io.github.freya022.pokedex.bot")

            components {
                enable = true
            }

            textCommands {
                enable = false
            }
        }
    }
}
