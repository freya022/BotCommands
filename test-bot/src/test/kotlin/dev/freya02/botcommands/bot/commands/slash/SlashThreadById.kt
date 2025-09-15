package dev.freya02.botcommands.bot.commands.slash

import dev.freya02.botcommands.jda.ktx.messages.reply_
import dev.freya02.botcommands.jda.ktx.retrieve.retrieveThreadChannelByIdOrNull
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption

@Command
class SlashThreadById : ApplicationCommand() {
    @JDASlashCommand(name = "thread_by_id")
    suspend fun execute(event: GuildSlashEvent, @SlashOption id: String) {
        val threadChannel = event.guild.retrieveThreadChannelByIdOrNull(id.toLong())
        event.reply_(threadChannel?.asMention.toString(), ephemeral = true).queue()
    }
}
