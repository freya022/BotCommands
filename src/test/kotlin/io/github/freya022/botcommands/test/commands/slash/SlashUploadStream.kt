package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.utils.FileUpload
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Command
class SlashUploadStream : ApplicationCommand() {
    @JDASlashCommand(name = "upload_stream")
    suspend fun execute(event: GuildSlashEvent) {
        event.deferReply(true).queue()

        val out = ByteArrayOutputStream()
        val written = withContext(Dispatchers.IO) {
            val bufferedImage = BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR)
            val graphics = bufferedImage.createGraphics()
            graphics.fillRect(0, 0, 100, 100)
            ImageIO.write(bufferedImage, "png", out).also {
                graphics.dispose()
            }
        }
        require(written)

        val bytes = out.toByteArray()

        FileUpload.fromData(bytes, "img.png").use { upload ->
            val embed = Embed {
                image = "attachment://img.png"
            }

            event.hook.sendMessageEmbeds(embed)
                .setFiles(upload)
                .await()
        }
    }
}