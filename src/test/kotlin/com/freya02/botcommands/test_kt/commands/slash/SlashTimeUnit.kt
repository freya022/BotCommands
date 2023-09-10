package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.components.builder.bindTo
import com.freya02.botcommands.api.components.event.ButtonEvent
import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

private const val TIME_UNIT_BUTTON_NAME = "SlashTimeUnit: timeUnit"

@Command
class SlashTimeUnit(private val componentsService: Components) : ApplicationCommand() {
    @JDASlashCommand(name = "time_unit")
    fun onSlashTimeUnit(
        event: GuildSlashEvent,
        @SlashOption(usePredefinedChoices = true) timeUnit: TimeUnit,
        @SlashOption(usePredefinedChoices = true) chronoUnit: ChronoUnit
    ) {
        val button = componentsService.persistentButton(ButtonStyle.PRIMARY, "TimeUnit: ${timeUnit.name}") {
            oneUse = true
            bindTo(::onTimeUnitClicked, timeUnit)
        }
        event.reply_("${timeUnit.name} / ${chronoUnit.name}", components = button.into(), ephemeral = true).queue()
    }

    @JDAButtonListener(TIME_UNIT_BUTTON_NAME)
    fun onTimeUnitClicked(event: ButtonEvent, unit: TimeUnit) {
        event.reply_("Time unit is ${unit.name}!", ephemeral = true).queue()
    }
}