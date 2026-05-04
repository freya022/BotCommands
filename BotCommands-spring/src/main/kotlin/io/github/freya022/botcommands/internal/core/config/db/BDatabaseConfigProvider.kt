package io.github.freya022.botcommands.internal.core.config.db

import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import io.github.freya022.botcommands.api.core.config.BDatabaseConfig
import io.github.freya022.botcommands.api.core.config.BDatabaseConfigConfigurer
import io.github.freya022.botcommands.internal.core.config.AbstractConfigProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@ConditionalOnClass(BDatabaseConfig::class)
@ConditionalOnProperty("botcommands.database.enable", matchIfMissing = true)
internal open class BDatabaseConfigProvider : AbstractConfigProvider() {

    @Bean
    @Primary
    internal open fun bDatabaseConfig(
        configuration: BotCommandsDatabaseConfiguration,
        configurers: List<BDatabaseConfigConfigurer>,
    ): BDatabaseConfig {
        return BDatabaseConfig.builder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()
    }

    @Bean
    internal open fun registeringDatabaseConfigurer(config: BDatabaseConfig): BConfigConfigurer {
        return object : BConfigConfigurer {
            override fun configure(builder: BConfigBuilder) {
                builder.registerModule(config)
            }
        }
    }
}
