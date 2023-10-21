package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.TextOption
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Command
class TextTimeUnit : TextCommand() {
    @JDATextCommand(path = ["time_unit"])
    fun onTextTimeUnit(
        event: BaseCommandEvent,
        @TextOption timeUnit: TimeUnit,
        @TextOption chronoUnit: ChronoUnit
    ) = event.respond("${timeUnit.name} / ${chronoUnit.name}").queue()
}