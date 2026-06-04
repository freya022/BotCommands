package dev.freya02.botcommands.bot.commands.message

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.reply_
import dev.freya02.botcommands.jda.ktx.requests.awaitUnit
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.core.utils.getSignature
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType.*
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.data.DataPath

@Command
class MessageContextGetJson {

    @JDAMessageCommand(
        name = "Get raw JSON",
        contexts = [GUILD, BOT_DM, PRIVATE_CHANNEL],
        integrationTypes = [IntegrationType.USER_INSTALL],
    )
    suspend fun onMessageGetRawJson(
        event: GlobalMessageEvent,
        @ContextOption message: Message
    ) {
        val dataObject = event.rawData
            ?: return event.reply_("${JDABuilder::setEventPassthrough.getSignature(returnType = true, source = false)} needs to be enabled", ephemeral = true).awaitUnit()

        val messageObject = DataPath.getObject(dataObject, "d.data.resolved.messages").getObject(message.id)
        val upload = FileUpload.fromData(messageObject.toPrettyString().encodeToByteArray(), "message-${message.id}.json")
        event.reply_(files = [upload], ephemeral = true).await()
    }
}
