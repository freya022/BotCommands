package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.events.LoadEvent
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.events.PreLoadEvent
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata
import kotlinx.coroutines.runBlocking
import kotlin.time.DurationUnit
import kotlin.time.measureTime

internal abstract class AbstractBotCommandsBootstrap(protected val config: BConfig) : BotCommandsBootstrap {
    protected val logger = objectLogger()

    internal fun init() {
        measure("Scanned reflection metadata") {
            ReflectionMetadata.runScan(config, this)
        }
    }

    internal fun loadContext() = runBlocking {
        measure("Completed BotCommands loading events") {
            serviceContainer.getService<BContextImpl>().apply {
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
    }

    protected inline fun measure(desc: String, block: () -> Unit) {
        measureTime(block).also {
            logger.trace { "$desc in ${it.toString(DurationUnit.MILLISECONDS, 2)}" }
        }
    }
}