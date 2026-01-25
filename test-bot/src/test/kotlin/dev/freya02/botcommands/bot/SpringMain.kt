package dev.freya02.botcommands.bot

import dev.freya02.botcommands.bot.config.Environment
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.io.path.absolutePathString

@SpringBootApplication(
        scanBasePackages = [
            "dev.freya02.botcommands.bot",
        ]
)
open class SpringMain

private val logger by lazy { KotlinLogging.logger { } }

fun main(args: Array<String>) {
    System.setProperty("logging.config", Environment.logbackConfigPath.absolutePathString())
    logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }

    runApplication<SpringMain>(*args)
}
