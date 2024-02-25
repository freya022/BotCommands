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
class SlashNsfw : ApplicationCommand(), GlobalApplicationCommandsDeclaration {
    @JDASlashCommand(name = "nsfw_annotated")
    @TopLevelSlashCommandData(nsfw = true)
    fun onSlashNsfw(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("nsfw", function = ::onSlashNsfw) {
            nsfw = true
        }
    }
}