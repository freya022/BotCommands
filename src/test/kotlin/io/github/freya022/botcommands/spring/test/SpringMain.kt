package io.github.freya022.botcommands.spring.test

import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import io.github.freya022.botcommands.api.core.annotations.EnableBotCommands
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.lang.management.ManagementFactory

@EnableBotCommands
@SpringBootApplication(
        scanBasePackages = [
            "io.github.freya022.botcommands.test",
            "doc"
        ]
)
class SpringMain

private val logger = KotlinLogging.logger { }

fun main(args: Array<String>) {
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

    runApplication<SpringMain>(*args)
}