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
    internal open fun bConfig(configuration: BotCommandsCoreConfiguration, configurers: List<BConfigConfigurer>): BConfig =
        BConfigBuilder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()

    @Bean
    @Primary
    internal open fun bDebugConfig(configuration: BotCommandsDebugConfiguration, configurers: List<BDebugConfigConfigurer>): BDebugConfig =
        BDebugConfigBuilder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()

    @Bean
    @Primary
    internal open fun bServiceConfig(configuration: BotCommandsServiceConfiguration, configurers: List<BServiceConfigConfigurer>): BServiceConfig =
        BServiceConfigBuilder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()

    @Bean
    @Primary
    internal open fun bDatabaseConfig(configuration: BotCommandsDatabaseConfiguration, configurers: List<BDatabaseConfigConfigurer>): BDatabaseConfig =
        BDatabaseConfigBuilder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()

    @Bean
    @Primary
    internal open fun bTextConfig(configuration: BotCommandsTextConfiguration, configurers: List<BTextConfigConfigurer>): BTextConfig =
        BTextConfigBuilder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()

    @Bean
    @Primary
    internal open fun bLocalizationConfig(configuration: BotCommandsLocalizationConfiguration, configurers: List<BLocalizationConfigConfigurer>): BLocalizationConfig =
        BLocalizationConfigBuilder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()

    @Bean
    @Primary
    internal open fun bApplicationConfig(configuration: BotCommandsApplicationConfiguration, configurers: List<BApplicationConfigConfigurer>): BApplicationConfig =
        BApplicationConfigBuilder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()

    @Bean
    @Primary
    internal open fun bComponentsConfig(configuration: BotCommandsComponentsConfiguration, configurers: List<BComponentsConfigConfigurer>): BComponentsConfig =
        BComponentsConfigBuilder()
            .applyConfig(configuration)
            .configure(configurers)
            .build()

    @Bean
    internal open fun bCoroutineScopesConfig(configurers: List<BCoroutineScopesConfigConfigurer>): BCoroutineScopesConfig =
        BCoroutineScopesConfigBuilder().configure(configurers).build()

    private fun <T : Any> T.configure(configurers: List<BConfigurer<T>>) = apply {
        configurers.forEach { configConfigurer -> configConfigurer.configure(this) }
    }
}