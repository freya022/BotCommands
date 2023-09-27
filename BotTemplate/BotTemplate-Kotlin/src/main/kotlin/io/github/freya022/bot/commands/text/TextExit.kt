package io.github.freya022.bot.commands.text

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.RequireOwner
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.Hidden
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption
import mu.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger { }

@Command
class TextExit : TextCommand() {
    @Hidden
    @RequireOwner
    @JDATextCommand(name = "exit")
    fun onTextExit(event: BaseCommandEvent, @TextOption reason: String?) {
        logger.warn("Exiting for reason: $reason")

        event.reactSuccess()
            .mapToResult()
            .queue { exitProcess(0) }
    }
}