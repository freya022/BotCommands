package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandDeclarationFilter
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.DeclarationFilter
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.core.conditions.RequiredIntents
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.requests.GatewayIntent

@BService
@RequiredIntents(GatewayIntent.GUILD_MEMBERS)
class BigGuildDeclarationFilter : CommandDeclarationFilter {
    override fun filter(guild: Guild, path: CommandPath, commandId: String?): Boolean {
        return guild.memberCount > 10 // not big, for testing yk
    }
}

@Command
@RequiredIntents(GatewayIntent.GUILD_MEMBERS)
class SlashDeclarationFilter : ApplicationCommand() {
    @JDASlashCommand(name = "declaration_filter")
    @DeclarationFilter(BigGuildDeclarationFilter::class)
    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    suspend fun onSlashDeclarationFilter(event: GuildSlashEvent) {
        event.reply_("Works, guild members: ${event.guild.memberCount}", ephemeral = true).await()
    }
}