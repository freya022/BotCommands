package io.github.freya022.botcommands.internal.commands.application.cache.provider

import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.factory.ApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.commands.application.cache.factory.FileApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.commands.application.cache.factory.MemoryApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.utils.reference
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.io.path.Path
import kotlin.io.path.isWritable
import kotlin.io.path.pathString

private val logger = KotlinLogging.loggerOf<ApplicationCommandsCache>()

//TODO in-memory/in-file/in-database storage
// Perhaps the configuration would look like `commandCache = FileCommandCache(baseDir)`
// In-memory storage should not be configurable, as it is used as a fallback for missing write permissions
@BService
@Configuration
internal open class ApplicationCommandsCacheFactoryProvider {
    @Lazy // Due to JDA requirement
    @Bean
    @BService
    internal open fun applicationCommandsCacheFactory(jda: JDA, applicationConfig: BApplicationConfig): ApplicationCommandsCacheFactory {
        val dataDirectory = applicationConfig.commandCachePath ?: run {
            val appDataDirectory = when {
                "Windows" in System.getProperty("os.name") -> System.getenv("appdata")
                else -> "/var/tmp"
            }
            Path(appDataDirectory).resolve("BotCommands")
        }

        if (!dataDirectory.isWritable()) {
            // Don't use absolutePathString in case it also produces an exception
            logger.warn { "Cannot write to '${dataDirectory.pathString}', try setting a different path in ${BApplicationConfig::commandCachePath.reference}, falling back to an in-memory store" }
            return MemoryApplicationCommandsCacheFactory()
        }

        return FileApplicationCommandsCacheFactory(dataDirectory.resolve("ApplicationCommands-${jda.selfUser.applicationId}"))
    }
}
