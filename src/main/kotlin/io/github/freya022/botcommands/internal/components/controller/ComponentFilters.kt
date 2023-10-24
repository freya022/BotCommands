package io.github.freya022.botcommands.internal.components.controller

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

private val logger = KotlinLogging.logger { }

@BService
internal class ComponentFilters internal constructor(private val context: BContext) {
    private val filters: Map<String, ComponentInteractionFilter> =
        context.getInterfacedServices<ComponentInteractionFilter>().associateBy { it.javaClass.name }

    private inner class MissingFilterFilter(val qualifiedName: String) : ComponentInteractionFilter {
        override suspend fun isAcceptedSuspend(
            event: GenericComponentInteractionCreateEvent,
            handlerName: String?
        ): Boolean {
            logger.warn { "Ignoring component interaction due to missing filter: '$qualifiedName'" }
            event.reply_(context.getDefaultMessages(event).componentNotAllowedErrorMsg, ephemeral = true).await()
            return false
        }

        override fun toString(): String {
            return "MissingFilterFilter(qualifiedName='$qualifiedName')"
        }
    }

    internal fun getFilters(qualifiedNames: Array<out String>): List<ComponentInteractionFilter> {
        return qualifiedNames.map { filters[it] ?: return listOf(MissingFilterFilter(it)) }
    }
}