package com.freya02.botcommands.api.core

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.core.events.LoadEvent
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.ShutdownEvent
import kotlin.time.Duration.Companion.minutes

class BBuilder private constructor(configConsumer: ReceiverConsumer<BConfig>) {
    private val logger = Logging.getLogger()
    private val config = configConsumer.applyTo(BConfig())

    companion object {
        @JvmStatic
        fun newBuilder(configConsumer: ReceiverConsumer<BConfig>, manager: CoroutineEventManager = getDefaultManager()) {
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
            val context = BContextImpl(config, manager)

            if (context.ownerIds.isEmpty()) {
                logger.info("No owner ID specified, exceptions won't be sent to owners")
            }

            context.eventDispatcher.dispatchEvent(LoadEvent())
        }
    }
}