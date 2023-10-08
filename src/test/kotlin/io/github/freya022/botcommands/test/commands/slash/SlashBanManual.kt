package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.annotations.Test
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.core.entities.InputUser

@Command
class SlashBanManual : ApplicationCommand() {
    @Test(guildIds = [722891685755093072])
    @JDASlashCommand(name = "ban_annotated", defaultLocked = true, scope = CommandScope.GUILD)
    suspend fun onSlashBan(
        event: GuildSlashEvent,
        @SlashOption target: InputUser,
        @SlashOption reason: String = "Banned by ${event.user.asTag}"
    ) {
        event.reply_(
            """
            Banned ${target.asMention}
            By: ${event.user.asMention}
            Reason: $reason 
            """.trimIndent(), ephemeral = true
        ).mention().await()
    }

    @AppDeclaration
    fun declare(manager: GuildApplicationCommandManager) {
        manager.slashCommand("ban", scope = CommandScope.GUILD, ::onSlashBan) {
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