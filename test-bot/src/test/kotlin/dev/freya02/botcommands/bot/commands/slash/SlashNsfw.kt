package dev.freya02.botcommands.bot.commands.slash

import dev.freya02.botcommands.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData

@Command
class SlashNsfw : GlobalApplicationCommandProvider {
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
