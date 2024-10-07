package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.core.options.Option
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ICustomResolver
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.reflect.jvm.jvmErasure

@Resolver
class TextAttachmentResolver :
        ClassParameterResolver<TextAttachmentResolver, Attachment>(Attachment::class),
        ICustomResolver<TextAttachmentResolver, Attachment> {

    override suspend fun resolveSuspend(option: Option, event: Event): Attachment? {
        if (event !is MessageReceivedEvent) return null

        val command = option.executable
        val myIndex = command.allOptionsOrdered
            .filter { it.type.jvmErasure == Attachment::class }
            .indexOf(option)

        return event.message.attachments.getOrNull(myIndex)
    }
}

/**
 * Only a PoC for ordered options, required to bind attachments to the right place
 */
@Command
class TextAttachmentParameters : TextCommand() {

    @JvmInline
    value class MyAttachment(val attachment: Attachment)

    @JDATextCommandVariation(path = ["attachment_parameters"])
    fun onTextAttachmentParameters(event: BaseCommandEvent, specialAttachment: MyAttachment, anyAttachment: Attachment) {
        event.reply("""
            My attachment: ${specialAttachment.attachment.proxyUrl}
            Any attachment: ${anyAttachment.proxyUrl}
        """.trimIndent()).queue()
    }
}