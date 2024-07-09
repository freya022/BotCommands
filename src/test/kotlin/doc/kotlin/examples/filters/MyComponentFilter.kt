package doc.kotlin.examples.filters

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.ComponentInteractionRejectionHandler
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.test.switches.TestLanguage
import io.github.freya022.botcommands.test.switches.TestService
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@BService
@TestService
@TestLanguage(TestLanguage.Language.KOTLIN)
class MyComponentFilter(private val botOwners: BotOwners) : ComponentInteractionFilter<String> {
    override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
        if (event.channel.idLong == 932902082724380744 && event.user !in botOwners) {
            return "Only owners are allowed to use components in <#932902082724380744>"
        }
        return null
    }
}

@BService
@TestService
@TestLanguage(TestLanguage.Language.KOTLIN)
class MyComponentRejectionHandler : ComponentInteractionRejectionHandler<String> {
    override suspend fun handleSuspend(
        event: GenericComponentInteractionCreateEvent,
        handlerName: String?,
        userData: String
    ) {
        event.reply_(userData, ephemeral = true).await()
    }
}