package io.github.freya022.botcommands.api.core.config.application.cache

import io.github.freya022.botcommands.api.commands.application.diff.DiffEngine
import io.github.freya022.botcommands.api.core.config.DevConfig
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import io.github.freya022.botcommands.internal.core.config.ConfigurationValue

// DEV NOTE: Prefer reading these properties from [[ApplicationCommandsCacheFactory#cacheConfig]]
interface ApplicationCommandsCacheConfig {
    /**
     * Controls whether the old/new application commands data should be logged.
     */
    enum class LogDataIf {
        /**
         * Only logs the old/new application commands data if they require an update.
         */
        CHANGED,

        /**
         * Always logs the old/new application commands data.
         */
        ALWAYS,

        /**
         * Never logs the old/new application commands data.
         */
        NEVER
    }

    /**
     * Enables the library to compare local commands against Discord's command,
     * to check if application commands need to be updated.
     *
     * The default behavior is to compare the command data to what has been locally saved,
     * as it does not require any request, and is therefore way faster.
     *
     * The issue with local checks is that you could update commands on another machine,
     * while the other machine is not aware of it.
     *
     * Which is why you should use online checks during development,
     * but local checks in production, as they avoid requests and each machine doesn't have the same shards.
     *
     * **Note**: This does not enable you to run two dev instances simultaneously.
     *
     * Default: `false`
     *
     * Spring property: `botcommands.application.cache.checkOnline`
     */
    @ConfigurationValue(path = "botcommands.application.cache.checkOnline", defaultValue = "false")
    val checkOnline: Boolean

    /**
     * The diff engine to use when comparing old and new application commands,
     * to determine if commands needs to be updated.
     *
     * Only change this if necessary.
     *
     * Default: [DiffEngine.NEW]
     *
     * Spring property: `botcommands.application.cache.diffEngine`
     */
    @ConfigurationValue(path = "botcommands.application.cache.diffEngine", defaultValue = "new")
    val diffEngine: DiffEngine

    /**
     * Whether the raw JSON of the application commands should be logged on `TRACE` when the condition is met.
     *
     * Default: `LogDataIf.NEVER`
     *
     * Spring property: `botcommands.application.cache.logDataIf`
     */
    @ConfigurationValue(path = "botcommands.application.cache.logDataIf", defaultValue = "never")
    val logDataIf: LogDataIf
}

@ConfigDSL
sealed class ApplicationCommandsCacheConfigBuilder : ApplicationCommandsCacheConfig {
    @set:DevConfig
    override var checkOnline: Boolean = false
    @set:DevConfig
    override var diffEngine: DiffEngine = DiffEngine.NEW
    override var logDataIf: ApplicationCommandsCacheConfig.LogDataIf = ApplicationCommandsCacheConfig.LogDataIf.NEVER

    @JvmSynthetic
    internal abstract fun build(): ApplicationCommandsCacheConfig
}

internal abstract class BuiltApplicationCommandsCacheConfig(builder: ApplicationCommandsCacheConfigBuilder) : ApplicationCommandsCacheConfig {
    override val checkOnline = builder.checkOnline
    override val diffEngine = builder.diffEngine
    override val logDataIf = builder.logDataIf
}