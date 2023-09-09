package com.freya02.botcommands.test_kt.commands.text

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption
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