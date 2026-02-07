package dev.freya02.botcommands.jda.keepalive

import ch.qos.logback.classic.ClassicConstants
import dev.freya02.botcommands.jda.keepalive.api.JDAKeepAlive
import dev.freya02.botcommands.jda.keepalive.api.config.withJDAKeepAlive
import dev.freya02.botcommands.restarter.api.config.withRestarter
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.DevConfig
import kotlin.io.path.absolutePathString

fun main() {
    System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Config.configDirectory.resolve("logback-test.xml").absolutePathString())

    // For those reading this, specifying the agent on the command line is better
    // but build tool/IDE support sucks, so dynamic loading it is
    JDAKeepAlive.install()

    BotCommands.create {
        addSearchPath("dev.freya02.botcommands.jda.keepalive")

        applicationCommands {
            fileCache {
                @OptIn(DevConfig::class)
                checkOnline = true
            }
        }

        withRestarter(emptyArray())

        withJDAKeepAlive(cacheKey = "random text unique per instance")
    }
}
