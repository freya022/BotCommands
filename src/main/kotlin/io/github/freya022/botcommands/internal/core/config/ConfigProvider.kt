package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.config.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
internal open class ConfigProvider {
    @Bean
    @Primary
    internal open fun bConfig(
        coreConfiguration: BotCommandsCoreConfiguration, coreConfigurers: List<BConfigConfigurer>,
        debugConfiguration: BotCommandsDebugConfiguration, debugConfigurers: List<BDebugConfigConfigurer>,
        serviceConfiguration: BotCommandsServiceConfiguration, serviceConfigurers: List<BServiceConfigConfigurer>,
        databaseConfiguration: BotCommandsDatabaseConfiguration, databaseConfigurers: List<BDatabaseConfigConfigurer>,
        textConfiguration: BotCommandsTextConfiguration, textConfigurers: List<BTextConfigConfigurer>,
        localizationConfiguration: BotCommandsLocalizationConfiguration, localizationConfigurers: List<BLocalizationConfigConfigurer>,
        applicationConfiguration: BotCommandsApplicationConfiguration, applicationConfigurers: List<BApplicationConfigConfigurer>,
        componentsConfiguration: BotCommandsComponentsConfiguration, componentsConfigurers: List<BComponentsConfigConfigurer>,
        coroutineConfigurers: List<BCoroutineScopesConfigConfigurer>
    ): BConfig =
        BConfigBuilder()
            .applyConfig(coreConfiguration)
            .apply {
                debugConfig.applyConfig(debugConfiguration).configure(debugConfigurers)
                serviceConfig.applyConfig(serviceConfiguration).configure(serviceConfigurers)
                databaseConfig.applyConfig(databaseConfiguration).configure(databaseConfigurers)
                textConfig.applyConfig(textConfiguration).configure(textConfigurers)
                localizationConfig.applyConfig(localizationConfiguration).configure(localizationConfigurers)
                applicationConfig.applyConfig(applicationConfiguration).configure(applicationConfigurers)
                componentsConfig.applyConfig(componentsConfiguration).configure(componentsConfigurers)
                coroutineScopesConfig.configure(coroutineConfigurers)
            }
            .configure(coreConfigurers)
            .build()

    @Bean
    @Primary
    internal open fun bDebugConfig(config: BConfig): BDebugConfig = config.debugConfig

    @Bean
    @Primary
    internal open fun bServiceConfig(config: BConfig): BServiceConfig = config.serviceConfig

    @Bean
    @Primary
    internal open fun bDatabaseConfig(config: BConfig): BDatabaseConfig = config.databaseConfig

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
    @Primary
    internal open fun bComponentsConfig(config: BConfig): BComponentsConfig = config.componentsConfig

    @Bean
    internal open fun bCoroutineScopesConfig(config: BConfig): BCoroutineScopesConfig = config.coroutineScopesConfig

    private fun <T : Any> T.configure(configurers: List<BConfigurer<T>>) = apply {
        configurers.forEach { configConfigurer -> configConfigurer.configure(this) }
    }
}