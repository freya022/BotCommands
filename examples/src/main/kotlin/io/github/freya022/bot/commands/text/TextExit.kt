package io.github.freya022.bot.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.RequireOwner
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand
import io.github.freya022.botcommands.api.commands.prefixed.annotations.Hidden
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextOption
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger { }

@Command
class TextExit : TextCommand() {
    @Hidden
    @RequireOwner
    @JDATextCommand(path = ["exit"])
    fun onTextExit(event: BaseCommandEvent, @TextOption reason: String?) {
        logger.warn { "Exiting for reason: $reason" }

        event.reactSuccess()
            .mapToResult()
            .queue { exitProcess(0) }
    }
}