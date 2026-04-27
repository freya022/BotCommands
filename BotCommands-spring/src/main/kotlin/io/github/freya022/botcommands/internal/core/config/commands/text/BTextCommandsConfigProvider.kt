package io.github.freya022.botcommands.internal.core.config.commands.text

import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.BConfigConfigurer
import io.github.freya022.botcommands.api.core.config.BTextConfig
import io.github.freya022.botcommands.api.core.config.BTextConfigConfigurer
import io.github.freya022.botcommands.internal.core.config.AbstractConfigProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@ConditionalOnClass(BTextConfig::class)
@ConditionalOnProperty("botcommands.text.enable", matchIfMissing = true)
internal open class BTextCommandsConfigProvider : AbstractConfigProvider() {

    @Bean
    @Primary
    internal open fun bTextCommandsConfig(
        configuration: BotCommandsTextCommandsConfiguration,
        configurers: List<BTextConfigConfigurer>,
    ): BTextConfig {
        return BTextConfig.builder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()
    }

    @Bean
    internal open fun registeringTextCommandsConfigurer(config: BTextConfig): BConfigConfigurer {
        return object : BConfigConfigurer {
            override fun configure(builder: BConfigBuilder) {
                builder.registerModule(config)
            }
        }
    }
}
