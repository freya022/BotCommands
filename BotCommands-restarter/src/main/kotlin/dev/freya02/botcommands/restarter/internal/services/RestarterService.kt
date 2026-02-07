package dev.freya02.botcommands.restarter.internal.services

import dev.freya02.botcommands.restarter.api.config.restarterConfig
import dev.freya02.botcommands.restarter.internal.RestartListener
import dev.freya02.botcommands.restarter.internal.Restarter
import dev.freya02.botcommands.restarter.internal.annotations.RequiresRestarter
import dev.freya02.botcommands.restarter.internal.watcher.ClasspathWatcher
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService

@BService
@RequiresRestarter
internal class RestarterService {

    @BEventListener
    fun onPostLoad(event: PostLoadEvent) {
        val context = event.context
        val restartConfig = context.config.restarterConfig
        Restarter.instance.addListener(object : RestartListener {
            override fun beforeStop() {
                context.shutdownNow()
            }
        })
        ClasspathWatcher.initialize(restartConfig.restartDelay)
    }
}
