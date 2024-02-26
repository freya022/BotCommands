package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.events.LoadEvent
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.events.PreLoadEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.Version
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDAInfo
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

/**
 * Entry point for the BotCommands framework.
 *
 * Note: Building JDA before the framework will result in an error,
 * I strongly recommend that you create a service extending [JDAService],
 * learn more on [the wiki](https://freya022.github.io/BotCommands/3.X/setup/getting-started/from-scratch/#creating-a-jdaservice).
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
    @JvmSynthetic
    fun create(manager: CoroutineEventManager = getDefaultManager(), configConsumer: ReceiverConsumer<BConfigBuilder>): BContext {
        return build(manager, BConfigBuilder().apply(configConsumer).build())
    }

    private fun getDefaultManager(): CoroutineEventManager {
        val scope = getDefaultScope()
        return CoroutineEventManager(scope, 1.minutes)
    }

    private fun build(manager: CoroutineEventManager, config: BConfig): BContext {
        val mark = TimeSource.Monotonic.markNow()
        val context = runBlocking(manager.coroutineContext) {
            logger.debug { "Loading BotCommands ${BCInfo.VERSION} (${BCInfo.BUILD_TIME}) ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}" }
            Version.checkVersions()

            val context = BContextImpl(config, manager)

            if (context.ownerIds.isEmpty())
                logger.info { "No owner ID specified, exceptions won't be sent to owners" }
            if (config.disableExceptionsInDMs)
                logger.info { "Configuration disabled sending exception in bot owners DMs" }
            if (config.disableAutocompleteCache)
                logger.info { "Configuration disabled autocomplete cache, except forced caches" }
            if (!config.textConfig.usePingAsPrefix && config.textConfig.prefixes.isEmpty())
                logger.info { "Text commands will not work as ping-as-prefix is disabled and no prefix has been added" }

            context.serviceContainer.loadServices()

            context.setStatus(BContext.Status.PRE_LOAD)
            context.eventDispatcher.dispatchEvent(PreLoadEvent(context))

            context.setStatus(BContext.Status.LOAD)
            context.eventDispatcher.dispatchEvent(LoadEvent(context))

            context.setStatus(BContext.Status.POST_LOAD)
            context.eventDispatcher.dispatchEvent(PostLoadEvent(context))

            context.setStatus(BContext.Status.READY)
            context.eventDispatcher.dispatchEvent(BReadyEvent(context))

            context
        }
        val duration = mark.elapsedNow()
        logger.info { "Loaded BotCommands in ${duration.toString(DurationUnit.SECONDS, 3)}" }
        return context
    }
}
