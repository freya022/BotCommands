package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand

@Command
class TextException : TextCommand() {
    @JDATextCommand(path = ["exception"])
    suspend fun onTextException(event: BaseCommandEvent) {
        event.context.dispatchException("test no throwable", null)
        event.context.dispatchException("test no throwable, with context", null, mapOf("pi" to 3.14159))
        throw RuntimeException("test throwable")
    }
}