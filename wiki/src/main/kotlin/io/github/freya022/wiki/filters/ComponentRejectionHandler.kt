package io.github.freya022.wiki.filters

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.components.ComponentInteractionRejectionHandler
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@WikiLanguage(WikiLanguage.Language.KOTLIN)
// --8<-- [start:component_rejection_handler-kotlin]
@BService
class ComponentRejectionHandler : ComponentInteractionRejectionHandler<String/*(1)!*/> {
    override suspend fun handleSuspend(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?,
        userData: String
    ) {
        event.reply_(userData, ephemeral = true).await()
    }
}
// --8<-- [end:component_rejection_handler-kotlin]