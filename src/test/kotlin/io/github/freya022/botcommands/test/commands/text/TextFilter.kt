package io.github.freya022.botcommands.test.commands.text

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.Filter
import io.github.freya022.botcommands.api.commands.text.*
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.TextDeclaration
import io.github.freya022.botcommands.api.commands.text.builder.filter
import io.github.freya022.botcommands.test.filters.InVoiceChannel
import io.github.freya022.botcommands.test.filters.IsBotOwner
import io.github.freya022.botcommands.test.filters.IsGuildOwner

@Command
class TextFilter : TextCommand() {
    @Filter(InVoiceChannel::class)
    @JDATextCommand(path = ["filter_annotated"])
    suspend fun onTextFilter(event: BaseCommandEvent) {
        event.respond("OK").await()
    }

    @TextDeclaration
    fun declare(manager: TextCommandManager) {
        manager.textCommand("filter") {
            variation(::onTextFilter) {
                filters += (filter<IsBotOwner>() or filter<IsGuildOwner>()) and filter<InVoiceChannel>()
            }
        }
    }
}