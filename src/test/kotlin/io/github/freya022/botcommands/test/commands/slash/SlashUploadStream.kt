package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import net.dv8tion.jda.api.utils.FileUpload
import kotlin.io.path.Path
import kotlin.io.path.inputStream

@Command
@ConditionalService(SlashUploadStream.Companion::class)
class SlashUploadStream : ApplicationCommand() {
    @JDASlashCommand(name = "upload_stream")
    suspend fun execute(event: GuildSlashEvent) {
        Embed {
            description = "empty"
        }.let { event.replyEmbeds(it).setEphemeral(true).await() }

        Path("target", "apidocs", "resources", "glass.png")
                .inputStream()
                .let { FileUpload.fromData(it, "glass.png") }
                .use { upload ->
                    Embed {
                        image = "attachment://glass.png"
                    }.also { event.hook.editOriginalEmbeds(it).setFiles(upload).queue() }
                }
    }

    companion object : ConditionalServiceChecker {
        override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>) = "nope"
    }
}