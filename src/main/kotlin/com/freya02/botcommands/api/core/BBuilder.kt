package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.GlobalExceptionHandler
import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.SettingsProvider
import com.freya02.botcommands.api.core.BBuilder.Companion.newBuilder
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.events.LoadEvent
import com.freya02.botcommands.api.core.events.PostLoadEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.Version
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.events.session.ShutdownEvent
import kotlin.time.Duration.Companion.minutes

/**
 * The only class you'll need to initialize the framework.
 *
 * See `newBuilder` methods
 */
class BBuilder private constructor(configConsumer: ReceiverConsumer<BConfig>) {
    private val logger = KotlinLogging.logger { }
    private val config = configConsumer.applyTo(BConfig())

    /**
     * The only class you'll need to initialize the framework.
     *
     * See `newBuilder` methods
     */
    companion object {
        /**
         * See `newBuilder` methods
         */
        @JvmStatic
        fun newBuilder(configConsumer: ReceiverConsumer<BConfig>) {
            return newBuilder(configConsumer, getDefaultManager())
        }

        /**
         * Creates a new instance of the framework.
         *
         * Tip: If you wish to use JDA as a service, you can have a class annotated with `@BService(ServiceStart.READY)`,
         * starting it when [newBuilder] returns is also fine.
         *
         * Example - Main.kt:
         * ```kt
         * val scope = getDefaultScope()
         * val manager = CoroutineEventManager(scope, 1.minutes)
         * manager.listener<ShutdownEvent> {
         *     this.cancel() //"this" is a scope delegate
         * }
         *
         * BBuilder.newBuilder({
         *     addSearchPath("io.github.name.bot") //Change this
         *
         *     components {
         *         useComponents = true
         *     }
         *
         *     textCommands {
         *         usePingAsPrefix = true
         *     }
         * }, manager)
         * ```
         *
         * JDAService.kt:
         * ```kt
         * @BService(ServiceStart.READY) //Initializing JDA before the framework is ready will error.
         * class JDAService(config: Config, eventManager: IEventManager) {
         *     init {
         *         light(config.token, enableCoroutines = false /* required */) {
         *             //Configure JDA
         *
         *             setEventManager(eventManager) //Required
         *         }
         *     }
         * }
         * ```
         *
         * Additional interfaces can be implemented as services, see below:
         *
         * @see DefaultMessagesSupplier
         * @see SettingsProvider
         * @see GlobalExceptionHandler
         */
        @JvmSynthetic
        fun newBuilder(configConsumer: ReceiverConsumer<BConfig>, manager: CoroutineEventManager) {
            BBuilder(configConsumer).build(manager)
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

    private fun build(manager: CoroutineEventManager) {
        runBlocking(manager.coroutineContext) {
            Version.checkVersions()

            val context = BContextImpl(config, manager)

            if (context.ownerIds.isEmpty())
                logger.info("No owner ID specified, exceptions won't be sent to owners")
            if (config.disableExceptionsInDMs)
                logger.info("Configuration disabled sending exception in bot owners DMs")
            if (config.disableAutocompleteCache)
                logger.info("Configuration disabled autocomplete cache, except forced caches")

            val loadableServices = context.serviceContainer.loadableServices
            context.status = BContext.Status.PRE_LOAD
            context.serviceContainer.loadServices(loadableServices, ServiceStart.PRE_LOAD)

            context.status = BContext.Status.LOAD
            context.serviceContainer.loadServices(loadableServices, ServiceStart.DEFAULT)
            context.eventDispatcher.dispatchEvent(LoadEvent())

            context.status = BContext.Status.POST_LOAD
            context.serviceContainer.loadServices(loadableServices, ServiceStart.POST_LOAD)
            context.eventDispatcher.dispatchEvent(PostLoadEvent())

            context.status = BContext.Status.READY
            context.serviceContainer.loadServices(loadableServices, ServiceStart.READY)
        }
    }
}
