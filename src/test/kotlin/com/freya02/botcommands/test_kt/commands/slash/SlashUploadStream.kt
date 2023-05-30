package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.utils.FileUpload
import kotlin.io.path.Path
import kotlin.io.path.inputStream

@Command
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
}