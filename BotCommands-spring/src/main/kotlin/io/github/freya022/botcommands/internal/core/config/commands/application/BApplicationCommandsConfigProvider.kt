package io.github.freya022.botcommands.internal.core.config.commands.application

import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.api.core.config.BApplicationConfigConfigurer
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import io.github.freya022.botcommands.internal.core.config.AbstractConfigProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@ConditionalOnClass(BApplicationConfig::class)
@ConditionalOnProperty("botcommands.application.enable", matchIfMissing = true)
internal open class BApplicationCommandsConfigProvider : AbstractConfigProvider() {

    @Bean
    @Primary
    internal open fun bApplicationCommandsConfig(
        configuration: BotCommandsApplicationCommandsConfiguration,
        configurers: List<BApplicationConfigConfigurer>,
    ): BApplicationConfig {
        return BApplicationConfig.builder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()
    }

    @Bean
    internal open fun registeringApplicationCommandsConfigurer(config: BApplicationConfig): BConfigConfigurer {
        return object : BConfigConfigurer {
            override fun configure(builder: BConfigBuilder) {
                builder.registerModule(config)
            }
        }
    }
}
