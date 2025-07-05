package io.github.freya022.botcommands.test.commands.text

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.annotations.TextOption
import net.dv8tion.jda.api.entities.IMentionable

@Command
class TextMentionable : TextCommand() {
    @JDATextCommandVariation(path = ["mentionable"])
    suspend fun onTextMentionable(event: BaseCommandEvent, @TextOption mentionable: IMentionable) {
        event.respond(mentionable.id).await()
    }
}