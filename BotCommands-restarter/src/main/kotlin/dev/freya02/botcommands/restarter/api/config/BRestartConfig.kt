package dev.freya02.botcommands.restarter.api.config

import dev.freya02.botcommands.restarter.api.ExperimentalRestartApi
import dev.freya02.botcommands.restarter.internal.exceptions.throwInternal
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.config.IConfig
import io.github.freya022.botcommands.api.core.config.getConfigOrNull
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.core.config.ConfigDSL
import java.time.Duration as JavaDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

@ExperimentalRestartApi
interface RestarterConfigProps {

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
 * @see [RestarterConfigBuilder]
 */
@InjectedService
@ExperimentalRestartApi
interface RestarterConfig : IConfig, RestarterConfigProps {
    override val configType get() = RestarterConfig::class.java
}

@ExperimentalRestartApi
internal val BConfig.restarterConfig: RestarterConfig
    get() = getConfigOrNull<RestarterConfig>()
        ?: throwInternal("Attempted to fetch a configuration of a disabled feature")

/**
 * Builder of [RestarterConfig].
 *
 * @see [RestarterConfigBuilder.Companion.create]
 */
@ConfigDSL
@ExperimentalRestartApi
class RestarterConfigBuilder private constructor(
    override val startArgs: List<String>,
) : RestarterConfigProps {

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

    /**
     * Builds the [RestarterConfig], you can register the built configuration with [BConfigBuilder.withConfig].
     */
    fun build(): RestarterConfig = object : RestarterConfig {
        override val startArgs = this@RestarterConfigBuilder.startArgs
        override val restartDelay = this@RestarterConfigBuilder.restartDelay
    }

    companion object {

        /**
         * Creates a new [RestarterConfigBuilder], you must [build] it and [register][BConfigBuilder.withConfig] it.
         *
         * @param args The program arguments, they will be passed to the main method upon restarting
         */
        @JvmStatic
        fun create(args: Array<out String>): RestarterConfigBuilder {
            return RestarterConfigBuilder(args.toList())
        }
    }
}

/**
 * Registers the restarter module.
 *
 * @param args  The program arguments, they will be passed to the main method upon restarting
 * @param block A block for further configuration
 */
@ExperimentalRestartApi
fun BConfigBuilder.withRestarter(args: Array<out String>, block: RestarterConfigBuilder.() -> Unit = { }) {
    val config = RestarterConfigBuilder.create(args)
        .apply(block)
        .build()
    withConfig(config)
}
