package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.declaration.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.declaration.GlobalApplicationCommandsDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData

@Command
class SlashSubWithGroup : ApplicationCommand(), GlobalApplicationCommandsDeclaration {
    @TopLevelSlashCommandData
    @JDASlashCommand(name = "tag_annotated", subcommand = "send")
    fun onSlashSubWithGroup(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @JDASlashCommand(name = "tag_annotated", group = "manage", subcommand = "create")
    fun onSlashSubWithGroupManageCreate(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @JDASlashCommand(name = "tag_annotated", group = "manage", subcommand = "edit")
    fun onSlashSubWithGroupManageEdit(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("tag", function = null) {
            subcommand("send", ::onSlashSubWithGroup)

            subcommandGroup("manage") {
                this@subcommandGroup.subcommand("create", ::onSlashSubWithGroup)

                this@subcommandGroup.subcommand("edit", ::onSlashSubWithGroup)
            }
        }
    }
}