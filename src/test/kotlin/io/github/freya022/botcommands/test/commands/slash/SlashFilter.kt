package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Filter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.and
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.builder.filter
import io.github.freya022.botcommands.api.commands.application.or
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.test.filters.InVoiceChannel
import io.github.freya022.botcommands.test.filters.IsBotOwner
import io.github.freya022.botcommands.test.filters.IsGuildOwner

@Command
class SlashFilter : ApplicationCommand() {
    @Filter(InVoiceChannel::class)
    @JDASlashCommand(name = "filter_annotated")
    suspend fun onSlashFilter(event: GuildSlashEvent) {
        event.reply_("OK", ephemeral = true).await()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("filter", function = ::onSlashFilter) {
            filters += (filter<IsBotOwner>() or filter<IsGuildOwner>()) and filter<InVoiceChannel>()
        }
    }
}