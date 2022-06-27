package com.freya02.botcommands.core.api

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.core.api.config.BConfig
import com.freya02.botcommands.internal.BContextImpl
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.cancel
import net.dv8tion.jda.api.events.ShutdownEvent
import kotlin.time.Duration.Companion.minutes

class BBuilder private constructor(configConsumer: ReceiverConsumer<BConfig>) {
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
        val context = BContextImpl(config, manager)

        println()
    }
}