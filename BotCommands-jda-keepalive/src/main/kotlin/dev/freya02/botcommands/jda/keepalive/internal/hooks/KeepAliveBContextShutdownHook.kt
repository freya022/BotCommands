package dev.freya02.botcommands.jda.keepalive.internal.hooks

import dev.freya02.botcommands.jda.keepalive.api.config.JDAKeepAliveConfig
import dev.freya02.botcommands.jda.keepalive.internal.JDABuilderSession
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.getConfigOrNull
import io.github.freya022.botcommands.internal.core.BContextImpl

internal class KeepAliveBContextShutdownHook : BContextImpl.ShutdownHook {
    override fun onShutdown(
        context: BContext,
        originalCall: () -> Unit,
        afterShutdown: () -> Unit,
    ) {
        val config = context.config.getConfigOrNull<JDAKeepAliveConfig>()
        if (config == null) {
            originalCall()
        } else {
            val key = config.cacheKey
            val session = JDABuilderSession.getSession(key)
            session.onScheduleShutdownSignal(originalCall, afterShutdown)
        }
    }
}
