package io.github.freya022.botcommands.test.commands.text

import dev.minn.jda.ktx.coroutines.await
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
import io.github.freya022.botcommands.test.filters.InVoiceChannel
import io.github.freya022.botcommands.test.filters.IsBotOwner
import io.github.freya022.botcommands.test.filters.IsGuildOwner
import io.github.freya022.botcommands.test.switches.TestService

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