package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.test.services.Disabled

@Disabled
@Command
class SlashBanManual : ApplicationCommand(), GuildApplicationCommandProvider {
    @Test(guildIds = [722891685755093072])
    @JDASlashCommand(name = "ban_annotated")
    @TopLevelSlashCommandData(defaultLocked = true, scope = CommandScope.GUILD)
    suspend fun onSlashBan(
        event: GuildSlashEvent,
        @SlashOption target: InputUser,
        @SlashOption reason: String = "Banned by ${event.user.name}"
    ) {
        event.reply_(
            """
            Banned ${target.asMention}
            By: ${event.user.asMention}
            Reason: $reason 
            """.trimIndent(), ephemeral = true
        ).mention().await()
    }

    override fun declareGuildApplicationCommands(manager: GuildApplicationCommandManager) {
        manager.slashCommand("ban", function = ::onSlashBan) {
            description = "Get banned"

            isDefaultLocked = true

            option("target") {
                description = "User to ban"
            }

            option("reason") {
                description = "The ban reason"
            }
        }
    }
}