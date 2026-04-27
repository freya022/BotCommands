package doc.kotlin.examples.filters

import dev.freya02.botcommands.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@BService
class MyTextCommandFilter : TextCommandFilter {

    override val global: Boolean get() = true

    override suspend fun checkSuspend(
        event: MessageReceivedEvent,
        commandVariation: TextCommandVariation,
        args: String
    ): String? {
        if (event.channel.idLong != 722891685755093076) {
            event.message.reply("Can only run commands in <#722891685755093076>").await()
            return "Wrong channel"
        }
        return null
    }
}
