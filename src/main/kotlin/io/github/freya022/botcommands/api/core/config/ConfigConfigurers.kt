package io.github.freya022.botcommands.api.core.config

sealed interface BConfigurer<T : Any> {
    fun configure(builder: T)
}

/**
 * Configurer for [BConfig].
 */
interface BConfigConfigurer : BConfigurer<BConfigBuilder>

/**
 * Configurer for [BConfig].
 */
interface BDebugConfigConfigurer : BConfigurer<BDebugConfigBuilder>

/**
 * Configurer for [BServiceConfig].
 */
interface BServiceConfigConfigurer : BConfigurer<BServiceConfigBuilder>

/**
 * Configurer for [BDatabaseConfig].
 */
interface BDatabaseConfigConfigurer : BConfigurer<BDatabaseConfigBuilder>

/**
 * Configurer for [BLocalizationConfig].
 */
interface BLocalizationConfigConfigurer : BConfigurer<BLocalizationConfigBuilder>

/**
 * Configurer for [BTextConfig].
 */
interface BTextConfigConfigurer : BConfigurer<BTextConfigBuilder>

/**
 * Configurer for [BApplicationConfig].
 */
interface BApplicationConfigConfigurer : BConfigurer<BApplicationConfigBuilder>

/**
 * Configurer for [BComponentsConfig].
 */
interface BComponentsConfigConfigurer : BConfigurer<BComponentsConfigBuilder>

/**
 * Configurer for [BCoroutineScopesConfig].
 */
interface BCoroutineScopesConfigConfigurer : BConfigurer<BCoroutineScopesConfigBuilder>