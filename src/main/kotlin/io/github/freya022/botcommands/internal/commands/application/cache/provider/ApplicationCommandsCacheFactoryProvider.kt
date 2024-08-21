package io.github.freya022.botcommands.internal.commands.application.cache.provider

import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.core.config.application.cache.DatabaseApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.config.application.cache.FileApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.factory.ApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.commands.application.cache.factory.FileApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.commands.application.cache.factory.MemoryApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.commands.application.cache.factory.NullApplicationCommandsCacheFactory
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.io.path.isWritable
import kotlin.io.path.pathString

private val logger = KotlinLogging.loggerOf<ApplicationCommandsCache>()

@BService
@Configuration
internal open class ApplicationCommandsCacheFactoryProvider {
    @Lazy // Due to JDA requirement
    @Bean
    @BService
    internal open fun applicationCommandsCacheFactory(jda: JDA, applicationConfig: BApplicationConfig): ApplicationCommandsCacheFactory {
        val cacheConfig = applicationConfig.cache ?: return NullApplicationCommandsCacheFactory

        when (cacheConfig) {
            is FileApplicationCommandsCacheConfig -> {
                val dataDirectory = cacheConfig.path
                if (!dataDirectory.isWritable()) {
                    // Don't use absolutePathString in case it also produces an exception
                    logger.warn { "Cannot write to '${dataDirectory.pathString}', try setting a different path in ${BApplicationConfigBuilder::fileCache.shortSignatureNoSrc}, falling back to an in-memory store" }
                    return MemoryApplicationCommandsCacheFactory(cacheConfig)
                }

                return FileApplicationCommandsCacheFactory(cacheConfig, jda.selfUser.applicationIdLong)
            }
            is DatabaseApplicationCommandsCacheConfig -> TODO()
            else -> throwInternal("Unsupported cache config: $cacheConfig")
        }
    }
}
