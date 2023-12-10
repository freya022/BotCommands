package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN)
// --8<-- [start:ping-kotlin]
@Command
class SlashPingKotlin : ApplicationCommand() {
    // Default scope is global, guild-only (GUILD_NO_DM)
    @JDASlashCommand(name = "ping", description = "Pong!")
    suspend fun onSlashPing(event: GuildSlashEvent) {
        event.deferReply(true).queue()

        val ping = event.jda.getRestPing().await()
        event.hook.editOriginal("Pong! $ping ms").await()
    }
}
// --8<-- [end:ping-kotlin]

@WikiCommandProfile(WikiCommandProfile.Profile.KOTLIN_DSL)
// --8<-- [start:ping-kotlin_dsl]
@Command
class SlashPingKotlinDsl {
    suspend fun onSlashPing(event: GuildSlashEvent) {
        event.deferReply(true).queue()

        val ping = event.jda.getRestPing().await()
        event.hook.editOriginal("Pong! $ping ms").await()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        // Default scope is global, guild-only (GUILD_NO_DM)
        manager.slashCommand("ping", function = ::onSlashPing) {
            description = "Pong!"
        }
    }
}
// --8<-- [end:ping-kotlin_dsl]