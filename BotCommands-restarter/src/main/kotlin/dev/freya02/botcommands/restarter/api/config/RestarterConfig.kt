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
 * Configuration for the restarter feature.
 *
 * To enable this feature, a configuration of it must be registered.
 *
 * @see [RestarterConfig.builder]
 * @see [registerRestarter]
 */
@InjectedService
@ExperimentalRestartApi
interface RestarterConfig : IConfig, RestarterConfigProps {

    override val configType get() = RestarterConfig::class.java

    companion object {
        /**
         * Creates a new [RestarterConfigBuilder], you must [build] it and [register][BConfigBuilder.registerModule] it.
         *
         * @param args The program arguments, they will be passed to the main method upon restarting
         */
        @JvmStatic
        fun builder(args: Array<out String>): RestarterConfigBuilder {
            return RestarterConfigBuilder.create(args)
        }
    }
}

@ExperimentalRestartApi
internal val BConfig.restarterConfig: RestarterConfig
    get() = getConfigOrNull<RestarterConfig>()
        ?: throwInternal("Attempted to fetch a configuration of a disabled feature")

/**
 * Builder of [RestarterConfig].
 *
 * @see [RestarterConfig.builder]
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
     * Builds the [RestarterConfig], you can register the built configuration with [BConfigBuilder.registerModule].
     */
    fun build(): RestarterConfig = object : RestarterConfig {
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

/**
 * Registers the restarter module, enabling the feature.
 *
 * @param args  The program arguments, they will be passed to the main method upon restarting
 * @param block A block for further configuration
 *
 * @throws IllegalStateException If the module was already registered
 */
@ExperimentalRestartApi
fun BConfigBuilder.registerRestarter(args: Array<out String>, block: RestarterConfigBuilder.() -> Unit = { }) {
    val config = RestarterConfigBuilder.create(args)
        .apply(block)
        .build()
    registerModule(config)
}
