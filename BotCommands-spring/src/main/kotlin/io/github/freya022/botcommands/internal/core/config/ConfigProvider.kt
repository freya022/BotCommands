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
        databaseConfiguration: BotCommandsDatabaseConfiguration, databaseConfigurers: List<BDatabaseConfigConfigurer>,
        appEmojisConfiguration: BotCommandsAppEmojisConfiguration, appEmojisConfigurers: List<BAppEmojisConfigConfigurer>,
        textConfiguration: BotCommandsTextConfiguration, textConfigurers: List<BTextConfigConfigurer>,
        localizationConfiguration: BotCommandsLocalizationConfiguration, localizationConfigurers: List<BLocalizationConfigConfigurer>,
        applicationConfiguration: BotCommandsApplicationConfiguration, applicationConfigurers: List<BApplicationConfigConfigurer>,
        coroutineConfigurers: List<BCoroutineScopesConfigConfigurer>,
    ): BConfig =
        BConfigBuilder()
            .applyConfig(coreConfiguration)
            .apply {
                eventManagerConfig.applyConfig(eventManagerConfiguration).configure(eventManagerConfigurers)
                databaseConfig.applyConfig(databaseConfiguration).configure(databaseConfigurers)
                appEmojisConfig.applyConfig(appEmojisConfiguration).configure(appEmojisConfigurers)
                textConfig.applyConfig(textConfiguration).configure(textConfigurers)
                localizationConfig.applyConfig(localizationConfiguration).configure(localizationConfigurers)
                applicationConfig.applyConfig(applicationConfiguration).configure(applicationConfigurers)
                coroutineScopesConfig.configure(coroutineConfigurers)
            }
            .configure(coreConfigurers)
            .build()

    @Bean
    @Primary
    internal open fun bEventManagerConfig(config: BConfig): BEventManagerConfig = config.eventManagerConfig

    @Bean
    @Primary
    internal open fun bDatabaseConfig(config: BConfig): BDatabaseConfig = config.databaseConfig

    @Bean
    @Primary
    internal open fun bAppEmojisConfig(config: BConfig): BAppEmojisConfig = config.appEmojisConfig

    @Bean
    @Primary
    internal open fun bTextConfig(config: BConfig): BTextConfig = config.textConfig

    @Bean
    @Primary
    internal open fun bLocalizationConfig(config: BConfig): BLocalizationConfig = config.localizationConfig

    @Bean
    @Primary
    internal open fun bApplicationConfig(config: BConfig): BApplicationConfig = config.applicationConfig

    @Bean
    internal open fun bCoroutineScopesConfig(config: BConfig): BCoroutineScopesConfig = config.coroutineScopesConfig
}
