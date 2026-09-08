package dev.freya02.botcommands.bot.commands.slash

import dev.freya02.botcommands.jda.ktx.components.AttachmentUpload
import dev.freya02.botcommands.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.create
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.interactions.FileType
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

@Command
class SlashFileTypes(
    private val modals: Modals
) : GlobalApplicationCommandProvider {

    fun onSlashFileTypesModal(event: GuildSlashEvent) {
        event.replyModal(modals.create("File types") {
            label("JSON") {
                child = AttachmentUpload("json") {
                    fileTypes += "json"
                }
            }

            bindTo { modalEvent ->
                val jsonAttachment = modalEvent.getValue("json")!!.asAttachmentList.first()
                modalEvent.reply_("File size: ${jsonAttachment.size}", ephemeral = true).queue()
            }
        }).queue()
    }

    fun onSlashFileTypesJson(event: GuildSlashEvent, jsonAttachment: Attachment) {
        event.reply_("File size: ${jsonAttachment.size}", ephemeral = true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("file_types", function = null) {
            integrationTypes = enumSetOf(IntegrationType.USER_INSTALL)
            contexts = enumSetOf(InteractionContextType.GUILD)

            subcommand("modal", ::onSlashFileTypesModal)

            subcommand("json", ::onSlashFileTypesJson) {
                option("jsonAttachment", optionName = "json") {
                    fileTypes += "json"
                }
            }
        }
    }
}
