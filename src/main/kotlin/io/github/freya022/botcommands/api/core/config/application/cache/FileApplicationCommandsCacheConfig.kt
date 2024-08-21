package io.github.freya022.botcommands.api.core.config.application.cache

import io.github.freya022.botcommands.internal.core.config.ConfigurationValue
import io.github.freya022.botcommands.internal.core.config.IgnoreDefaultValue
import java.nio.file.Path

interface FileApplicationCommandsCacheConfig : ApplicationCommandsCacheConfig {
    /**
     * Path at which the application commands cache would be saved to.
     *
     * Each application has a folder inside it, meaning you can safely share this folder with other applications.
     *
     * Default: `%AppData%/BotCommands` on Windows, `/var/tmp/BotCommands` on Unix
     *
     * Spring property: `botcommands.application.cache.file.path`
     */
    @IgnoreDefaultValue
    @ConfigurationValue(path = "botcommands.application.cache.file.path")
    val path: Path
}

class FileApplicationCommandsCacheConfigBuilder internal constructor(
    override val path: Path,
) : ApplicationCommandsCacheConfigBuilder(),
    FileApplicationCommandsCacheConfig {

    @JvmSynthetic
    override fun build(): FileApplicationCommandsCacheConfig = object : BuiltApplicationCommandsCacheConfig(this),
                                                                        FileApplicationCommandsCacheConfig {
        override val path: Path = this@FileApplicationCommandsCacheConfigBuilder.path
    }
}