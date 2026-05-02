package io.github.freya022.botcommands.api.core.config.application.cache

interface DatabaseApplicationCommandsCacheConfig : ApplicationCommandsCacheConfig

class DatabaseApplicationCommandsCacheConfigBuilder internal constructor(

) : ApplicationCommandsCacheConfigBuilder(),
    DatabaseApplicationCommandsCacheConfig {

    @JvmSynthetic
    override fun build(): DatabaseApplicationCommandsCacheConfig = object : BuiltApplicationCommandsCacheConfig(this),
                                                                            DatabaseApplicationCommandsCacheConfig {

    }
}