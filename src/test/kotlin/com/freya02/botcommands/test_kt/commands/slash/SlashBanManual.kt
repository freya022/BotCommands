package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.User

@CommandMarker //Not unused
class SlashBanManual {
    @CommandMarker //Since IJ won't shut up about unused functions even though i am referencing below
    suspend fun onSlashBan(event: GuildSlashEvent, target: User, reason: String = "Banned by ${event.user.asTag}") {
        event.reply_(
            """
            Banned ${target.asMention}
            By: ${event.user.asMention}
            Reason: $reason 
            """.trimIndent(), ephemeral = true
        ).mention().await()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("ban", scope = CommandScope.GLOBAL_NO_DM) {
            description = "Get banned"

            option("target") {
                description = "User to ban"
            }

            option("reason") {
                description = "The ban reason"
            }

            function = ::onSlashBan
        }
    }
}