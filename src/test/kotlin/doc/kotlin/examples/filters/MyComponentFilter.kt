package doc.kotlin.examples.filters

import doc.java.examples.filters.MyComponentRejectionHandler
import io.github.freya022.botcommands.api.components.GlobalComponentInteractionFilter
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.test.switches.TestLanguage
import io.github.freya022.botcommands.test.switches.TestService
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

@BService
@TestService
@TestLanguage(TestLanguage.Language.KOTLIN)
class MyComponentFilter(
    private val rejectionHandler: MyComponentRejectionHandler,
    private val botOwners: BotOwners,
) : GlobalComponentInteractionFilter {

    override suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? {
        if (event.channel.idLong == 932902082724380744 && event.user !in botOwners) {
            rejectionHandler.handle(event, "Only owners are allowed to use components in <#932902082724380744>")
            return "Not an owner"
        }
        return null
    }
}
