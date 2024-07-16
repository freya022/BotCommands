package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.events.LoadEvent
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.events.PreLoadEvent
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.Version
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDAInfo
import kotlin.time.DurationUnit
import kotlin.time.measureTime

internal abstract class AbstractBotCommandsBootstrap(protected val config: BConfig) : BotCommandsBootstrap {
    protected val logger = objectLogger()

    internal fun init() {
        logger.debug { "Loading BotCommands ${BCInfo.VERSION} (${BCInfo.BUILD_TIME}) ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}" }
        Version.checkVersions()

        if (config.disableExceptionsInDMs)
            logger.info { "Configuration disabled sending exception in bot owners DMs" }
        if (config.applicationConfig.disableAutocompleteCache)
            logger.info { "Configuration disabled autocomplete cache, except forced caches" }
        if (!config.textConfig.usePingAsPrefix && config.textConfig.prefixes.isEmpty())
            logger.info { "Text commands will not work as ping-as-prefix is disabled and no prefix has been added" }

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