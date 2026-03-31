package io.github.freya022.botcommands.internal.core.config.modals

import io.github.freya022.botcommands.api.core.config.*
import io.github.freya022.botcommands.internal.core.config.AbstractConfigProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@ConditionalOnClass(BModalsConfigBuilder::class)
@ConditionalOnProperty("botcommands.modals.enable", matchIfMissing = true)
internal open class BModalsConfigProvider : AbstractConfigProvider() {

    @Bean
    @Primary
    internal open fun bModalsConfig(
        configuration: BotCommandsModalsConfiguration,
        configurers: List<BModalsConfigConfigurer>,
    ): BModalsConfig {
        return BModalsConfig.builder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()
    }

    @Bean
    internal open fun registeringModalsConfigurer(config: BModalsConfig): BConfigConfigurer {
        return object : BConfigConfigurer {
            override fun configure(builder: BConfigBuilder) {
                builder.registerModule(config)
            }
        }
    }
}
