package dev.freya02.botcommands.restarter.api.config

import dev.freya02.botcommands.restarter.api.BotCommandsRestarter
import dev.freya02.botcommands.restarter.api.annotations.ExperimentalRestartApi
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import java.time.Duration as JavaDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

@ExperimentalRestartApi
interface RestarterConfig {

    /**
     * The program arguments passed to the main function upon restarting.
     */
    val startArgs: List<String>

    /**
     * The time to wait before assuming all changes were compiled,
     * so the application can be restarted with the new changes.
     *
     * Default: 1 second
     */
    val restartDelay: Duration

    /**
     * Returns the time to wait before assuming all changes were compiled,
     * so the application can be restarted with the new changes.
     *
     * Default: 1 second
     */
    fun getRestartDelay(): JavaDuration = restartDelay.toJavaDuration()
}

/**
 * Builder of [RestarterConfig].
 *
 * @see [BotCommandsRestarter.initialize]
 */
@ConfigDSL
@ExperimentalRestartApi
class RestarterConfigBuilder private constructor(
    override val startArgs: List<String>,
) : RestarterConfig {

    override var restartDelay: Duration = 1.seconds

    /**
     * Sets the time to wait before assuming all changes were compiled,
     * so the application can be restarted with the new changes.
     *
     * Default: 1 second
     */
    fun setRestartDelay(delay: JavaDuration): RestarterConfigBuilder {
        this.restartDelay = delay.toKotlinDuration()
        return this
    }

    @JvmSynthetic
    internal fun build(): RestarterConfig = object : RestarterConfig {
        override val startArgs = this@RestarterConfigBuilder.startArgs
        override val restartDelay = this@RestarterConfigBuilder.restartDelay
    }

    internal companion object {

        @JvmSynthetic
        internal fun create(args: Array<out String>): RestarterConfigBuilder {
            return RestarterConfigBuilder(args.toList())
        }
    }
}
