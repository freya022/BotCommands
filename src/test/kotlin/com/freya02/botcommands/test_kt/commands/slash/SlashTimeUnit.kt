package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.parameters.enumResolver
import dev.minn.jda.ktx.messages.reply_
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Command
class SlashTimeUnit : ApplicationCommand() {
    @JDASlashCommand(name = "time_unit")
    fun onSlashTimeUnit(
        event: GuildSlashEvent,
        @SlashOption(usePredefinedChoices = true) timeUnit: TimeUnit,
        @SlashOption(usePredefinedChoices = true) chronoUnit: ChronoUnit
    ) = event.reply_("${timeUnit.name} / ${chronoUnit.name}", ephemeral = true).queue()

    @Resolver
    fun timeUnitResolver() = enumResolver<TimeUnit>(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES) { it.name.lowercase() }

    @Resolver
    fun chronoUnitResolver() = enumResolver<ChronoUnit>(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES) { it.name.lowercase() }
}