package io.github.freya022.botcommands.api.core.config

sealed interface BConfigurer<T : Any> {
    fun configure(builder: T)
}

//TODO default implementations to read from application.properties
interface BConfigConfigurer : BConfigurer<BConfigBuilder>

interface BDebugConfigConfigurer : BConfigurer<BDebugConfigBuilder>

interface BServiceConfigConfigurer : BConfigurer<BServiceConfigBuilder>

interface BDatabaseConfigConfigurer : BConfigurer<BDatabaseConfigBuilder>

interface BTextConfigConfigurer : BConfigurer<BTextConfigBuilder>

interface BApplicationConfigConfigurer : BConfigurer<BApplicationConfigBuilder>

interface BComponentsConfigConfigurer : BConfigurer<BComponentsConfigBuilder>

interface BCoroutineScopesConfigConfigurer : BConfigurer<BCoroutineScopesConfigBuilder>