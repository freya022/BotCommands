package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.annotations.Test
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.User

@Command
class SlashBanManual : ApplicationCommand() {
    @Test(guildIds = [722891685755093072])
    @JDASlashCommand(name = "ban_annotated", defaultLocked = true, scope = CommandScope.GUILD)
    suspend fun onSlashBan(
        event: GuildSlashEvent,
        @AppOption target: User,
        @AppOption reason: String = "Banned by ${event.user.asTag}"
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