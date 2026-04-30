package io.github.freya022.botcommands.internal.core.config.emojis

import io.github.freya022.botcommands.api.core.config.BAppEmojisConfig
import io.github.freya022.botcommands.api.core.config.BAppEmojisConfigConfigurer
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import io.github.freya022.botcommands.internal.core.config.AbstractConfigProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@ConditionalOnClass(BAppEmojisConfig::class)
@ConditionalOnProperty("botcommands.app.emojis.enable", matchIfMissing = true)
internal open class BAppEmojisConfigProvider : AbstractConfigProvider() {

    @Bean
    @Primary
    internal open fun bAppEmojisConfig(
        configuration: BotCommandsAppEmojisConfiguration,
        configurers: List<BAppEmojisConfigConfigurer>,
    ): BAppEmojisConfig {
        return BAppEmojisConfig.builder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()
    }

    @Bean
    internal open fun registeringAppEmojisConfigurer(config: BAppEmojisConfig): BConfigConfigurer {
        return object : BConfigConfigurer {
            override fun configure(builder: BConfigBuilder) {
                builder.registerModule(config)
            }
        }
    }
}
