package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.config.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
internal open class ConfigProvider : AbstractConfigProvider() {
    @Bean
    @Primary
    internal open fun bConfig(
        coreConfiguration: BotCommandsCoreConfiguration, coreConfigurers: List<BConfigConfigurer>,
        eventManagerConfiguration: BotCommandsEventManagerConfiguration, eventManagerConfigurers: List<BEventManagerConfigConfigurer>,
        coroutineConfigurers: List<BCoroutineScopesConfigConfigurer>,
    ): BConfig =
        BConfigBuilder()
            .applyConfig(coreConfiguration)
            .apply {
                eventManagerConfig.applyConfig(eventManagerConfiguration).configure(eventManagerConfigurers)
                coroutineScopesConfig.configure(coroutineConfigurers)
            }
            .configure(coreConfigurers)
            .build()

    @Bean
    @Primary
    internal open fun bEventManagerConfig(config: BConfig): BEventManagerConfig = config.eventManagerConfig

    @Bean
    internal open fun bCoroutineScopesConfig(config: BConfig): BCoroutineScopesConfig = config.coroutineScopesConfig
}
