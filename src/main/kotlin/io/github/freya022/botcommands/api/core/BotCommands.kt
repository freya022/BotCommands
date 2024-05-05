package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.annotations.EnableBotCommands
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.service.DefaultBotCommandsBootstrap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.DurationUnit
import kotlin.time.measureTimedValue

/**
 * Entry point for the BotCommands framework.
 *
 * The only requirement for a basic bot is a service extending [JDAService],
 * learn more on [the wiki](https://freya022.github.io/BotCommands/3.X/setup/getting-started/from-scratch/#creating-a-jdaservice).
 *
 * ### Spring support
 *
 * Spring users must use [@EnableBotCommands][EnableBotCommands] instead.
 *
 * @see BService @BService
 * @see InterfacedService @InterfacedService
 * @see Command @Command
 */
object BotCommands {
    private val logger = KotlinLogging.logger { }

    /**
     * Creates a new instance of the framework.
     *
     * @return The context for the newly created framework instance,
     * while this is returned, using it *usually* is not a good idea,
     * your architecture should rely on [dependency injection](https://freya022.github.io/BotCommands/3.X/using-botcommands/dependency-injection/)
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
     * your architecture should rely on [dependency injection](https://freya022.github.io/BotCommands/3.X/using-botcommands/dependency-injection/)
     * and events instead.
     *
     * @see BotCommands
     */
    @Deprecated("Event manager is set by a service implementing ICoroutineEventManagerSupplier")
    @JvmSynthetic
    fun create(manager: CoroutineEventManager, configConsumer: ReceiverConsumer<BConfigBuilder>): BContext {
        return build(BConfigBuilder().apply(configConsumer).build())
    }

    /**
     * Creates a new instance of the framework.
     *
     * @return The context for the newly created framework instance,
     * while this is returned, using it *usually* is not a good idea,
     * your architecture should rely on [dependency injection](https://freya022.github.io/BotCommands/3.X/using-botcommands/dependency-injection/)
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
            val bootstrap = DefaultBotCommandsBootstrap(config)
            bootstrap.injectAndLoadServices()
            bootstrap.loadContext()
            bootstrap.serviceContainer.getService<BContext>()
        }
        logger.info { "Loaded BotCommands in ${duration.toString(DurationUnit.SECONDS, 3)}" }

        return context
    }
}
