package io.github.freya022.bot.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.bot.commands.WikiProfile

@WikiProfile(WikiProfile.Profile.KOTLIN)
// --8<-- [start:ping_kotlin]
@Command
class SlashPingKotlin : ApplicationCommand() {
    // Default scope is global, guild-only (GUILD_NO_DM)
    @JDASlashCommand(name = "ping", description = "Pong!")
    suspend fun onSlashPing(event: GuildSlashEvent) {
        event.deferReply(true).queue()

        val ping = event.jda.getRestPing().await()
        event.hook.editOriginal("Pong! $ping ms").queue()
    }
}
// --8<-- [end:ping_kotlin]

@WikiProfile(WikiProfile.Profile.KOTLIN_DSL)
// --8<-- [start:ping_kotlin_dsl]
@Command
class SlashPingKotlinDsl {
    suspend fun onSlashPing(event: GuildSlashEvent) {
        event.deferReply(true).queue()

        val ping = event.jda.getRestPing().await()
        event.hook.editOriginal("Pong! $ping ms").queue()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        // Default scope is global, guild-only (GUILD_NO_DM)
        manager.slashCommand("ping", function = ::onSlashPing) {
            description = "Pong!"
        }
    }
}
// --8<-- [end:ping_kotlin_dsl]