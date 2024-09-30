package io.github.freya022.botcommands.api.core.config

/**
 * Base interface for runtime configuration, only usable with Spring, see sub-interfaces.
 */
sealed interface BConfigurer<T : Any> {
    fun configure(builder: T)
}

/**
 * Configurer for [BConfig].
 * 
 * Only usable with Spring.
 */
interface BConfigConfigurer : BConfigurer<BConfigBuilder>

/**
 * Configurer for [BDatabaseConfig].
 *
 * Only usable with Spring.
 */
interface BDatabaseConfigConfigurer : BConfigurer<BDatabaseConfigBuilder>

/**
 * Configurer for [BLocalizationConfig].
 *
 * Only usable with Spring.
 */
interface BLocalizationConfigConfigurer : BConfigurer<BLocalizationConfigBuilder>

/**
 * Configurer for [BTextConfig].
 *
 * Only usable with Spring.
 */
interface BTextConfigConfigurer : BConfigurer<BTextConfigBuilder>

/**
 * Configurer for [BApplicationConfig].
 *
 * Only usable with Spring.
 */
interface BApplicationConfigConfigurer : BConfigurer<BApplicationConfigBuilder>

/**
 * Configurer for [BModalsConfig].
 *
 * Only usable with Spring.
 */
interface BModalsConfigConfigurer : BConfigurer<BModalsConfigBuilder>

/**
 * Configurer for [BComponentsConfig].
 *
 * Only usable with Spring.
 */
interface BComponentsConfigConfigurer : BConfigurer<BComponentsConfigBuilder>

/**
 * Configurer for [BCoroutineScopesConfig].
 *
 * Only usable with Spring.
 */
interface BCoroutineScopesConfigConfigurer : BConfigurer<BCoroutineScopesConfigBuilder>