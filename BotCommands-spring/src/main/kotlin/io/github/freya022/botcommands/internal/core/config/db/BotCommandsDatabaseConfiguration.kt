package io.github.freya022.botcommands.internal.core.config.db

import io.github.freya022.botcommands.api.core.config.BDatabaseConfig
import io.github.freya022.botcommands.api.core.config.BDatabaseConfigBuilder
import io.github.freya022.botcommands.api.core.config.BDatabaseConfigProps
import io.github.freya022.botcommands.api.core.config.DevConfig
import io.github.freya022.botcommands.internal.core.config.AbstractBotCommandsConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration as JavaDuration
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

@ConditionalOnClass(BDatabaseConfig::class)
@ConfigurationProperties(prefix = "botcommands.database", ignoreUnknownFields = false)
internal class BotCommandsDatabaseConfiguration(
    override val dumpLongTransactions: Boolean = false,
    override val logQueries: Boolean = false,
    override val logQueryParameters: Boolean = true,
    queryLogThreshold: JavaDuration? = null
) : AbstractBotCommandsConfiguration(), BDatabaseConfigProps {
    override val queryLogThreshold: Duration = queryLogThreshold?.toKotlinDuration() ?: Duration.INFINITE
}

@OptIn(DevConfig::class)
internal fun BDatabaseConfigBuilder.applyConfig(configuration: BotCommandsDatabaseConfiguration) = apply {
    dumpLongTransactions = configuration.dumpLongTransactions
    logQueries = configuration.logQueries
    logQueryParameters = configuration.logQueryParameters
    queryLogThreshold = configuration.queryLogThreshold
}
