package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.BCInfo
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.Version
import com.freya02.botcommands.internal.core.events.LoadEvent
import com.freya02.botcommands.internal.core.events.PostLoadEvent
import com.freya02.botcommands.internal.throwUser
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.events.session.ShutdownEvent
import kotlin.time.Duration.Companion.minutes

class BBuilder private constructor(configConsumer: ReceiverConsumer<BConfig>) {
    private val logger = KotlinLogging.logger { }
    private val config = configConsumer.applyTo(BConfig())

    companion object {
        @JvmStatic
        fun newBuilder(configConsumer: ReceiverConsumer<BConfig>) {
            return newBuilder(configConsumer, getDefaultManager())
        }

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
            logger.debug("Loading BotCommands ${BCInfo.VERSION} ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}")
            checkVersions()

            val context = BContextImpl(config, manager)

            if (context.ownerIds.isEmpty()) {
                logger.info("No owner ID specified, exceptions won't be sent to owners")
            } else if (config.devMode) {
                logger.info("Developer mode enabled, exceptions won't be sent to owners")
            }

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

    private fun checkVersions() {
        val requiredJdaVersionStr = BCInfo.BUILD_JDA_VERSION
        val requiredJdaVersion = Version.getOrNull(requiredJdaVersionStr) ?: let {
            logger.warn("Unrecognized built-with JDA version: $requiredJdaVersionStr")
            return
        }

        val currentJdaVersionStr = JDAInfo.VERSION
        val currentJdaVersion = Version.getOrNull(currentJdaVersionStr) ?: let {
            logger.warn("Unrecognized JDA version: $currentJdaVersionStr")
            return
        }

        if (currentJdaVersion < requiredJdaVersion) {
            throwUser("This bot is currently running JDA $currentJdaVersionStr but requires at least $requiredJdaVersionStr")
        }
    }
}