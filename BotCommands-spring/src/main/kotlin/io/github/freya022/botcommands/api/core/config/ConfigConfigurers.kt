package io.github.freya022.botcommands.api.core.config

/**
 * Base interface for runtime configuration, see sub-interfaces.
 */
sealed interface BConfigurer<T : Any> {
    fun configure(builder: T)
}

/**
 * Configurer for [BConfig].
 */
interface BConfigConfigurer : BConfigurer<BConfigBuilder>

/**
 * Configurer for [BEventManagerConfig].
 */
interface BEventManagerConfigConfigurer : BConfigurer<BEventManagerConfigBuilder>

/**
 * Configurer for [BDatabaseConfig].
 */
interface BDatabaseConfigConfigurer : BConfigurer<BDatabaseConfigBuilder>

/**
 * Configurer for [BLocalizationConfig].
 */
interface BLocalizationConfigConfigurer : BConfigurer<BLocalizationConfigBuilder>

/**
 * Configurer for [BAppEmojisConfig].
 */
interface BAppEmojisConfigConfigurer : BConfigurer<BAppEmojisConfigBuilder>

/**
 * Configurer for [BTextConfig].
 */
interface BTextConfigConfigurer : BConfigurer<BTextConfigBuilder>

/**
 * Configurer for [BApplicationConfig].
 */
interface BApplicationConfigConfigurer : BConfigurer<BApplicationConfigBuilder>

/**
 * Configurer for [BModalsConfig].
 */
interface BModalsConfigConfigurer : BConfigurer<BModalsConfigBuilder>

/**
 * Configurer for [BComponentsConfig].
 */
interface BComponentsConfigConfigurer : BConfigurer<BComponentsConfigBuilder>

/**
 * Configurer for [BCoroutineScopesConfig].
 */
interface BCoroutineScopesConfigConfigurer : BConfigurer<BCoroutineScopesConfigBuilder>

/**
 * Configurer for [LocalComponentsConfig].
 */
interface LocalComponentsConfigConfigurer : BConfigurer<LocalComponentsConfigBuilder>
