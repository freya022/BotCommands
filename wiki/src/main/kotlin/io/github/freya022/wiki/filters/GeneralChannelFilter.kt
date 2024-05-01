package io.github.freya022.wiki.filters

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:component_filter-kotlin]
@BService
class GeneralChannelFilter : ComponentInteractionFilter<String/*(1)!*/> {
    private val channelId = 722891685755093076

    // So we can apply this filter on specific components
    override val global: Boolean = false

    override suspend fun checkSuspend(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?
    ): String? {
        if (event.channelIdLong == channelId)
            return "This button can only be used in <#$channelId>"
        return null
    }
}
// --8<-- [end:component_filter-kotlin]