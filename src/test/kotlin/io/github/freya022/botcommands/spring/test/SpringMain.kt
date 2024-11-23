package io.github.freya022.botcommands.spring.test

import dev.reformator.stacktracedecoroutinator.jvm.DecoroutinatorJvmApi
import io.github.freya022.botcommands.test.config.Environment
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.lang.management.ManagementFactory
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess

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

    try {
        runApplication<SpringMain>(*args)
    } catch (e: Exception) {
        // Don't handle the exception sent by DevTools
        if (e.javaClass.name == "org.springframework.boot.devtools.restart.SilentExitExceptionHandler\$SilentExitException")
            return

        logger.catching(e)
        exitProcess(1)
    }
}