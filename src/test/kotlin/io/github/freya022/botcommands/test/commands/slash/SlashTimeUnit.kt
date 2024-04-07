package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.RequiresComponents
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.bindTo
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Command
@Dependencies(Components::class)
@RequiresComponents
class SlashTimeUnit(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "time_unit")
    suspend fun onSlashTimeUnit(
        event: GuildSlashEvent,
        @SlashOption(usePredefinedChoices = true) timeUnit: TimeUnit,
        @SlashOption(usePredefinedChoices = true) chronoUnit: ChronoUnit
    ) {
        val button = buttons.primary("TimeUnit: ${timeUnit.name}").persistent() {
            oneUse = true
            bindTo(::onTimeUnitClicked, timeUnit)
        }
        event.reply_("${timeUnit.name} / ${chronoUnit.name}", components = button.into(), ephemeral = true).queue()
    }

    @JDAButtonListener("SlashTimeUnit: timeUnit")
    fun onTimeUnitClicked(event: ButtonEvent, unit: TimeUnit) {
        event.reply_("Time unit is ${unit.name}!", ephemeral = true).queue()
    }
}