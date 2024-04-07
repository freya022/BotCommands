package io.github.freya022.botcommands.internal.core.config

import io.github.freya022.botcommands.api.core.config.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class ConfigProvider {
    @Bean
    fun bConfig(configurers: List<BConfigConfigurer>): BConfig =
        BConfigBuilder().configure(configurers).build()

    @Bean
    fun bDebugConfig(configurers: List<BDebugConfigConfigurer>): BDebugConfig =
        BDebugConfigBuilder().configure(configurers).build()

    @Bean
    fun bServiceConfig(configurers: List<BServiceConfigConfigurer>): BServiceConfig =
        BServiceConfigBuilder().configure(configurers).build()

    @Bean
    fun bDatabaseConfig(configurers: List<BDatabaseConfigConfigurer>): BDatabaseConfig =
        BDatabaseConfigBuilder().configure(configurers).build()

    @Bean
    fun bTextConfig(configurers: List<BTextConfigConfigurer>): BTextConfig =
        BTextConfigBuilder().configure(configurers).build()

    @Bean
    fun bApplicationConfig(configurers: List<BApplicationConfigConfigurer>): BApplicationConfig =
        BApplicationConfigBuilder().configure(configurers).build()

    @Bean
    fun bComponentsConfig(configurers: List<BComponentsConfigConfigurer>): BComponentsConfig =
        BComponentsConfigBuilder().configure(configurers).build()

    @Bean
    fun bCoroutineScopesConfig(configurers: List<BCoroutineScopesConfigConfigurer>): BCoroutineScopesConfig =
        BCoroutineScopesConfigBuilder().configure(configurers).build()

    private fun <T : Any> T.configure(configurers: List<BConfigurer<T>>) = apply {
        configurers.forEach { configConfigurer -> configConfigurer.configure(this) }
    }
}