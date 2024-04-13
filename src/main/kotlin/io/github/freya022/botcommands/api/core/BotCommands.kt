package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.annotations.EnableBotCommands
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.events.LoadEvent
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.events.PreLoadEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.Version
import io.github.freya022.botcommands.internal.core.service.DefaultServiceBootstrap
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDAInfo
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.time.measureTime

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
        val mark = TimeSource.Monotonic.markNow()
        val context = runBlocking {
            logger.debug { "Loading BotCommands ${BCInfo.VERSION} (${BCInfo.BUILD_TIME}) ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}" }
            Version.checkVersions()

            if (config.ownerIds.isEmpty())
                logger.info { "No owner ID specified, exceptions won't be sent to owners" }
            if (config.disableExceptionsInDMs)
                logger.info { "Configuration disabled sending exception in bot owners DMs" }
            if (config.disableAutocompleteCache)
                logger.info { "Configuration disabled autocomplete cache, except forced caches" }
            if (!config.textConfig.usePingAsPrefix && config.textConfig.prefixes.isEmpty())
                logger.info { "Text commands will not work as ping-as-prefix is disabled and no prefix has been added" }

            val serviceBootstrap = DefaultServiceBootstrap(config)

            measureTime {
                ReflectionMetadata.runScan(config, serviceBootstrap)
            }.also { logger.trace { "Reflection metadata took ${it.toString(DurationUnit.MILLISECONDS, 2)}" } }

            serviceBootstrap.serviceContainer.loadServices()
            serviceBootstrap.serviceContainer.getService<BContextImpl>().apply {
                setStatus(BContext.Status.PRE_LOAD)
                eventDispatcher.dispatchEvent(PreLoadEvent(this))

                setStatus(BContext.Status.LOAD)
                eventDispatcher.dispatchEvent(LoadEvent(this))

                setStatus(BContext.Status.POST_LOAD)
                eventDispatcher.dispatchEvent(PostLoadEvent(this))

                setStatus(BContext.Status.READY)
                eventDispatcher.dispatchEvent(BReadyEvent(this))
            }
        }
        val duration = mark.elapsedNow()
        logger.info { "Loaded BotCommands in ${duration.toString(DurationUnit.SECONDS, 3)}" }
        return context
    }
}
