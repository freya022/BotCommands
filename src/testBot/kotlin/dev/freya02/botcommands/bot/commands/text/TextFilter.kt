package dev.freya02.botcommands.bot.commands.text

import dev.freya02.botcommands.bot.filters.InVoiceChannel
import dev.freya02.botcommands.bot.filters.IsBotOwner
import dev.freya02.botcommands.bot.filters.IsGuildOwner
import dev.freya02.botcommands.bot.switches.TestService
import dev.freya02.botcommands.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Filter
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.and
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.builder.filter
import io.github.freya022.botcommands.api.commands.text.or
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider

@Command
@TestService
class TextFilter : TextCommand(), TextCommandProvider {
    @Filter(InVoiceChannel::class)
    @JDATextCommandVariation(path = ["filter_annotated"])
    suspend fun onTextFilter(event: BaseCommandEvent) {
        event.respond("OK").await()
    }

    override fun declareTextCommands(manager: TextCommandManager) {
        manager.textCommand("filter") {
            variation(::onTextFilter) {
                filters += (filter<IsBotOwner>() or filter<IsGuildOwner>()) and filter<InVoiceChannel>()
            }
        }
    }
}
