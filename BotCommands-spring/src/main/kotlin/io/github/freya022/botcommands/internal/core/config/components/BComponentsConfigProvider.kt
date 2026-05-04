package io.github.freya022.botcommands.internal.core.config.components

import io.github.freya022.botcommands.api.core.config.BComponentsConfig
import io.github.freya022.botcommands.api.core.config.BComponentsConfigConfigurer
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import io.github.freya022.botcommands.internal.core.config.AbstractConfigProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@ConditionalOnClass(BComponentsConfig::class)
@ConditionalOnProperty("botcommands.components.enable", matchIfMissing = true)
internal open class BComponentsConfigProvider : AbstractConfigProvider() {

    @Bean
    @Primary
    internal open fun bComponentsConfig(
        configuration: BotCommandsComponentsConfiguration,
        configurers: List<BComponentsConfigConfigurer>,
    ): BComponentsConfig {
        return BComponentsConfig.builder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()
    }

    @Bean
    internal open fun registeringComponentsConfigurer(config: BComponentsConfig): BConfigConfigurer {
        return object : BConfigConfigurer {
            override fun configure(builder: BConfigBuilder) {
                builder.registerModule(config)
            }
        }
    }
}
