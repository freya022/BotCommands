package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.core.utils.retrieveThreadChannelOrNull

@Command
class SlashThreadById : ApplicationCommand() {
    @JDASlashCommand(name = "thread_by_id")
    suspend fun execute(event: GuildSlashEvent, @SlashOption id: String) {
        val threadChannel = event.guild.retrieveThreadChannelOrNull(id.toLong())
        event.reply_(threadChannel?.asMention.toString(), ephemeral = true).queue()
    }
}