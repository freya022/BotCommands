package dev.freya02.botcommands.bot.commands.slash

import dev.freya02.botcommands.bot.filters.InVoiceChannel
import dev.freya02.botcommands.bot.filters.IsBotOwner
import dev.freya02.botcommands.bot.filters.IsGuildOwner
import dev.freya02.botcommands.bot.switches.TestService
import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Filter
import io.github.freya022.botcommands.api.commands.application.and
import io.github.freya022.botcommands.api.commands.application.builder.filter
import io.github.freya022.botcommands.api.commands.application.or
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand

@Command
@TestService
class SlashFilter : GlobalApplicationCommandProvider {
    @Filter(InVoiceChannel::class)
    @JDASlashCommand(name = "filter_annotated")
    suspend fun onSlashFilter(event: GuildSlashEvent) {
        event.reply_("OK", ephemeral = true).await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("filter", function = ::onSlashFilter) {
            filters += (filter<IsBotOwner>() or filter<IsGuildOwner>()) and filter<InVoiceChannel>()
        }
    }
}
