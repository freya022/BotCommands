package io.github.freya022.botcommands.spring.test

import io.github.freya022.botcommands.test.config.Environment
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.io.path.absolutePathString

@SpringBootApplication(
        scanBasePackages = [
            "io.github.freya022.botcommands.test",
            "doc"
        ]
)
open class SpringMain

private val logger by lazy { KotlinLogging.logger { } }

fun main(args: Array<String>) {
    System.setProperty("logging.config", Environment.logbackConfigPath.absolutePathString())
    logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }

    // No need to install stacktrace-decoroutinator
    // The stack traces are already enhanced in debug mode by the Java agent provided by IntelliJ
    // The IntelliJ debug agent also doesn't cause errors when exceptions happen after transforming reloaded classes

    runApplication<SpringMain>(*args)
}