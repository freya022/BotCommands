package doc.kotlin.examples.filters

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@BService
class MyComponentFilter(
    private val botOwners: BotOwners,
) : ComponentInteractionFilter {

    override val global: Boolean get() = true

    override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
        if (event.channel.idLong == 932902082724380744 && event.user !in botOwners) {
            event.reply_("Only owners are allowed to use components in <#932902082724380744>", ephemeral = true).await()
            return "Not an owner"
        }
        return null
    }
}
