package dev.freya02.botcommands.restarter.internal.services

import dev.freya02.botcommands.restarter.api.config.RestarterConfig
import dev.freya02.botcommands.restarter.internal.Restarter
import dev.freya02.botcommands.restarter.internal.annotations.RequiresRestarter
import io.github.freya022.botcommands.api.core.config.getConfigOrNull
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.events.BApplicationStartEvent
import io.github.freya022.botcommands.internal.core.hooks.ApplicationStartListener

@BService
@RequiresRestarter
internal class RestarterApplicationStartListener : ApplicationStartListener {

    override fun onApplicationStart(event: BApplicationStartEvent) {
        val restarterConfig = event.config.getConfigOrNull<RestarterConfig>()
        if (restarterConfig != null) {
            Restarter.initialize(restarterConfig.startArgs)
        }
    }
}
