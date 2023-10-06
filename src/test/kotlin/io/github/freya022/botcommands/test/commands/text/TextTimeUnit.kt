package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextOption
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Command
class TextTimeUnit : TextCommand() {
    @JDATextCommand(name = "time_unit")
    fun onTextTimeUnit(
        event: BaseCommandEvent,
        @TextOption timeUnit: TimeUnit,
        @TextOption chronoUnit: ChronoUnit
    ) = event.respond("${timeUnit.name} / ${chronoUnit.name}").queue()
}