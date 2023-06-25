package io.github.freya022.bot.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.concurrent.TimeUnit

@Resolver
class TimeUnitResolver :
    ParameterResolver<TimeUnitResolver, TimeUnit>(TimeUnit::class),
    SlashParameterResolver<TimeUnitResolver, TimeUnit> {

    override val optionType = OptionType.STRING

    // Sets the choices on the option, if opted-in with "usePredefinedChoices"
    override fun getPredefinedChoices(guild: Guild?): List<Command.Choice> =
        listOf(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS).map { unit ->
            Command.Choice(unit.name.lowercase().replaceFirstChar { it.uppercase() }, unit.name)
        }

    override suspend fun resolveSuspend(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): TimeUnit = TimeUnit.valueOf(optionMapping.asString)
}