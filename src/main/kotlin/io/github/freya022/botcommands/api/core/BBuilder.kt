package io.github.freya022.botcommands.api.core

import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.events.LoadEvent
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.events.PreLoadEvent
import io.github.freya022.botcommands.api.core.service.ServiceStart
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.Version
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.events.session.ShutdownEvent
import kotlin.time.Duration.Companion.minutes

/**
 * Entry point for the BotCommands framework.
 *
 * Note: Building JDA before the framework will result in an error,
 * I strongly recommend that you create a service extending [JDAService],
 * learn more on [the wiki](https://freya022.github.io/BotCommands/3.X/setup/getting-started/#creating-a-jdaservice).
 *
 * @see BService @BService
 * @see InterfacedService @InterfacedService
 * @see Command @Command
 */
class BBuilder private constructor(configConsumer: ReceiverConsumer<BConfigBuilder>) {
    private val logger = KotlinLogging.logger { }
    private val config = configConsumer.applyTo(BConfigBuilder()).build()

    /**
     * Entry point for the BotCommands framework.
     *
     * @see BBuilder
     */
    companion object {
        /**
         * Creates a new instance of the framework.
         *
         * @see BBuilder
         */
        @JvmStatic
        @JvmName("newBuilder")
        fun newBuilderJava(configConsumer: ReceiverConsumer<BConfigBuilder>): BContext {
            return newBuilder(configConsumer = configConsumer)
        }

        /**
         * Creates a new instance of the framework.
         *
         * @see BBuilder
         */
        @JvmSynthetic
        fun newBuilder(manager: CoroutineEventManager = getDefaultManager(), configConsumer: ReceiverConsumer<BConfigBuilder>): BContext {
            return BBuilder(configConsumer).build(manager)
        }

        private fun getDefaultManager(): CoroutineEventManager {
            val scope = getDefaultScope()
            return CoroutineEventManager(scope, 1.minutes).apply {
                listener<ShutdownEvent> {
                    scope.cancel()
                }
            }
        }
    }

    private fun build(manager: CoroutineEventManager): BContext {
        return runBlocking(manager.coroutineContext) {
            logger.debug { "Loading BotCommands ${BCInfo.VERSION} (${BCInfo.BUILD_TIME}) ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}" }
            Version.checkVersions()

            val context = BContextImpl(config, manager)

            if (context.ownerIds.isEmpty())
                logger.info { "No owner ID specified, exceptions won't be sent to owners" }
            if (config.disableExceptionsInDMs)
                logger.info { "Configuration disabled sending exception in bot owners DMs" }
            if (config.disableAutocompleteCache)
                logger.info { "Configuration disabled autocomplete cache, except forced caches" }

            context.serviceContainer.loadServices(ServiceStart.DEFAULT)

            context.setStatus(BContext.Status.PRE_LOAD)
            context.eventDispatcher.dispatchEvent(PreLoadEvent(context))

            context.setStatus(BContext.Status.LOAD)
            context.eventDispatcher.dispatchEvent(LoadEvent(context))

            context.setStatus(BContext.Status.POST_LOAD)
            context.eventDispatcher.dispatchEvent(PostLoadEvent(context))

            context.setStatus(BContext.Status.READY)
            context.serviceContainer.loadServices(ServiceStart.READY)
            context.eventDispatcher.dispatchEvent(BReadyEvent(context))

            context
        }
    }
}
