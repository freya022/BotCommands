package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData

@Command
class SlashNsfw : ApplicationCommand() {
    @JDASlashCommand(name = "nsfw_annotated")
    @TopLevelSlashCommandData(nsfw = true)
    fun onSlashNsfw(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("nsfw", function = ::onSlashNsfw) {
            nsfw = true
        }
    }
}