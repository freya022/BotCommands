package io.github.freya022.botcommands.api.core

import dev.freya02.botcommands.method.accessors.api.annotations.ExperimentalMethodAccessorsApi
import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.service.BCBotCommandsBootstrap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

/**
 * Entry point for the BotCommands framework.
 *
 * The only requirement for a basic bot is a service extending [JDAService],
 * learn more on [the wiki](https://bc.freya02.dev/3.X/setup/getting-started/#creating-a-jdaservice).
 *
 * ### Spring support
 * Spring users must not use this, the framework will autoconfigure itself.
 *
 * @see BService @BService
 * @see InterfacedService @InterfacedService
 * @see Command @Command
 */
object BotCommands {
    private val logger = KotlinLogging.logger { }

    /**
     * If enabled, instructs the framework to prefer using improved reflection calls, with the following benefits:
     * - Shorter stack traces in exceptions and the debugger
     * - No [InvocationTargetExceptions][java.lang.reflect.InvocationTargetException]
     * - Better performance
     *
     * This feature requires adding the `BotCommands-method-accessors-classfile` dependency
     * and *running* on Java 24+, if your bot doesn't fulfill these conditions, this method has no effect.
     */
    @ExperimentalMethodAccessorsApi
    @get:JvmStatic
    @get:JvmName("isPreferClassFileAccessors")
    var preferClassFileAccessors: Boolean = false
        private set

    /**
     * Instructs the framework to prefer using improved reflection calls, with the following benefits:
     * - Shorter stack traces in exceptions and the debugger
     * - No [InvocationTargetExceptions][java.lang.reflect.InvocationTargetException]
     * - Better performance
     *
     * This feature requires adding the `BotCommands-method-accessors-classfile` dependency
     * and *running* on Java 24+, if your bot doesn't fulfill these conditions, this method has no effect.
     */
    @JvmStatic
    @ExperimentalMethodAccessorsApi
    fun preferClassFileAccessors() {
        preferClassFileAccessors = true
    }

    /**
     * Creates a new instance of the framework.
     *
     * @return The context for the newly created framework instance,
     * while this is returned, using it *usually* is not a good idea,
     * your architecture should rely on [dependency injection](https://bc.freya02.dev/3.X/using-botcommands/dependency-injection/)
     * and events instead.
     *
     * @see BotCommands
     */
    @JvmStatic
    @JvmName("create")
    fun createJava(configConsumer: ReceiverConsumer<BConfigBuilder>): BContext {
        return create(configConsumer = configConsumer)
    }

    /**
     * Creates a new instance of the framework.
     *
     * @return The context for the newly created framework instance,
     * while this is returned, using it *usually* is not a good idea,
     * your architecture should rely on [dependency injection](https://bc.freya02.dev/3.X/using-botcommands/dependency-injection/)
     * and events instead.
     *
     * @see BotCommands
     */
    @JvmSynthetic
    fun create(configConsumer: BConfigBuilder.() -> Unit): BContext {
        return build(BConfigBuilder().apply(configConsumer).build())
    }

    private fun build(config: BConfig): BContext {
        val (context, duration) = measureTimedValue {
            val bootstrap = BCBotCommandsBootstrap(config)
            bootstrap.injectAndLoadServices()
            bootstrap.loadContext()
            bootstrap.serviceContainer.getService<BContext>()
        }
        logger.info { "Loaded BotCommands in ${duration.toString(DurationUnit.SECONDS, 3)}" }

        return context
    }
}
