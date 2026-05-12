package io.github.freya022.botcommands.internal.core.config.localization

import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import io.github.freya022.botcommands.api.core.config.BLocalizationConfig
import io.github.freya022.botcommands.api.core.config.BLocalizationConfigConfigurer
import io.github.freya022.botcommands.internal.core.config.AbstractConfigProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

// This configuration is always enabled
@Configuration
internal open class BLocalizationConfigProvider : AbstractConfigProvider() {

    @Bean
    @Primary
    internal open fun bLocalizationConfig(
        configuration: BotCommandsLocalizationConfiguration,
        configurers: List<BLocalizationConfigConfigurer>,
    ): BLocalizationConfig {
        return BLocalizationConfig.builder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()
    }

    @Bean
    internal open fun registeringLocalizationConfigurer(config: BLocalizationConfig): BConfigConfigurer {
        return object : BConfigConfigurer {
            override fun configure(builder: BConfigBuilder) {
                builder.registerModule(config)
            }
        }
    }
}
