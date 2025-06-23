package doc.kotlin.examples.filters

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

@BService
class MyApplicationCommandFilter : ApplicationCommandFilter {

    override val global: Boolean get() = true

    override suspend fun checkSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): String? {
        if (event.channel!!.idLong != 722891685755093076) {
            event.reply_("Can only run commands in <#722891685755093076>", ephemeral = true).await()
            return "Wrong channel"
        }
        return null
    }
}
