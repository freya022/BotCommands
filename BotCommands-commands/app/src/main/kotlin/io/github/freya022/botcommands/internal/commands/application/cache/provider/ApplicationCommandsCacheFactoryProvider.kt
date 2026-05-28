package io.github.freya022.botcommands.internal.commands.application.cache.provider

import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.core.config.BDatabaseConfig
import io.github.freya022.botcommands.api.core.config.application.cache.DatabaseApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.config.application.cache.FileApplicationCommandsCacheConfig
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Lazy
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.commands.application.cache.ApplicationCommandsCache
import io.github.freya022.botcommands.internal.commands.application.cache.factory.*
import io.github.freya022.botcommands.internal.core.db.DatabaseSchemaHelper
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isWritable
import kotlin.io.path.pathString

private val logger = KotlinLogging.loggerOf<ApplicationCommandsCache>()

@BService
@Configuration
internal open class ApplicationCommandsCacheFactoryProvider {
    @Lazy // Due to JDA requirement
    @Bean
    @BService
    internal open fun applicationCommandsCacheFactory(jda: JDA, applicationConfig: BApplicationConfig, serviceContainer: ServiceContainer): ApplicationCommandsCacheFactory {
        val cacheConfig = applicationConfig.cache
            ?: return NullApplicationCommandsCacheFactory // Logged in [[BApplicationConfigBuilder#build]]

        when (cacheConfig) {
            is FileApplicationCommandsCacheConfig -> {
                fun Path.absolutePathStringOrFallback(): String {
                    return runCatching {
                        absolutePathString()
                    }.getOrElse {
                        logger.warn(it) { "Unable to resolve absolute path of '$pathString'" }
                        pathString
                    }
                }

                val dataDirectory = cacheConfig.path
                if (dataDirectory.exists() && !dataDirectory.isWritable()) {
                    // Don't use absolutePathString in case it also produces an exception
                    logger.warn { "Cannot write to '${dataDirectory.absolutePathStringOrFallback()}', try setting a different path in ${BApplicationConfigBuilder::fileCache.shortSignatureNoSrc}, falling back to an in-memory store" }
                    return MemoryApplicationCommandsCacheFactory(cacheConfig)
                } else if (!dataDirectory.parent.isWritable()) {
                    // Don't use absolutePathString in case it also produces an exception
                    logger.warn { "Cannot create directory at '${dataDirectory.absolutePathStringOrFallback()}', try setting a different path in ${BApplicationConfigBuilder::fileCache.shortSignatureNoSrc}, falling back to an in-memory store" }
                    return MemoryApplicationCommandsCacheFactory(cacheConfig)
                }

                logger.debug { "Using file-based application commands cache @ ${dataDirectory.absolutePathStringOrFallback()}" }
                return FileApplicationCommandsCacheFactory(cacheConfig, jda.selfUser.applicationIdLong)
            }
            is DatabaseApplicationCommandsCacheConfig -> {
                val database = serviceContainer.getServiceOrNull<Database>()
                if (database == null) {
                    logger.warn { "Cannot use a database as application commands cache as no database is present, see ${classRef<BDatabaseConfig>()}" }
                    return MemoryApplicationCommandsCacheFactory(cacheConfig)
                }

                // Can't move to method due to reflection loading `Database` (which is `compileOnly`)
                val isSchemaValid = runBlocking {
                    DatabaseSchemaHelper.validateSchemaVersion(
                        logger,
                        database,
                        schemaName = "bc_commands_app",
                        // If the build script has 3.0.0-alpha.5_DEV, use the next release version, in this case 3.0.0-alpha.6
                        latestVersion = "4.0.0-alpha.1", // Change in the latest migration script too
                        featureName = "application commands",
                        fallbackMessage = "Falling back to an in-memory store."
                    )
                }
                if (!isSchemaValid) {
                    return MemoryApplicationCommandsCacheFactory(cacheConfig)
                }

                logger.debug { "Using database-based application commands cache" }
                return DatabaseApplicationCommandsCacheFactory(cacheConfig, database, jda.selfUser.applicationIdLong)
            }
            else -> throwInternal("Unsupported cache config: $cacheConfig")
        }
    }
}
