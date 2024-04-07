package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BConfig
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.events.LoadEvent
import io.github.freya022.botcommands.api.core.events.PostLoadEvent
import io.github.freya022.botcommands.api.core.events.PreLoadEvent
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.core.service.SpringServiceBootstrap
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDAInfo
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger { }

@Component
internal class BotCommandsInitializer(
    private val config: BConfig,
    private val serviceBootstrap: SpringServiceBootstrap
) : InitializingBean {
    override fun afterPropertiesSet() = runBlocking {
        val mark = TimeSource.Monotonic.markNow()

        logger.debug { "Loading BotCommands ${BCInfo.VERSION} (${BCInfo.BUILD_TIME}) ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}" }
        Version.checkVersions()

        if (config.ownerIds.isEmpty())
            logger.info { "No owner ID specified, exceptions won't be sent to owners" }
        if (config.disableExceptionsInDMs)
            logger.info { "Configuration disabled sending exception in bot owners DMs" }
        if (config.disableAutocompleteCache)
            logger.info { "Configuration disabled autocomplete cache, except forced caches" }
        if (!config.textConfig.usePingAsPrefix && config.textConfig.prefixes.isEmpty())
            logger.info { "Text commands will not work as ping-as-prefix is disabled and no prefix has been added" }

        serviceBootstrap.serviceContainer.getService<BContextImpl>().apply {
            setStatus(BContext.Status.PRE_LOAD)
            eventDispatcher.dispatchEvent(PreLoadEvent(this))

            setStatus(BContext.Status.LOAD)
            eventDispatcher.dispatchEvent(LoadEvent(this))

            setStatus(BContext.Status.POST_LOAD)
            eventDispatcher.dispatchEvent(PostLoadEvent(this))

            setStatus(BContext.Status.READY)
            eventDispatcher.dispatchEvent(BReadyEvent(this))
        }
        val duration = mark.elapsedNow()
        logger.info { "Loaded BotCommands in ${duration.toString(DurationUnit.SECONDS, 3)}" }
    }
}