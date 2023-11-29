package io.github.freya022.wiki.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import io.github.freya022.botcommands.api.localization.to
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.enumResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.toHumanName
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import io.github.freya022.wiki.switches.WikiDetailProfile
import io.github.freya022.wiki.switches.WikiLanguage
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.concurrent.TimeUnit

@Suppress("unused")
object TimeUnitResolverSimplified {
    @WikiLanguage(WikiLanguage.Language.KOTLIN)
    @WikiDetailProfile(WikiDetailProfile.Profile.SIMPLIFIED)
    // --8<-- [start:time_unit_resolver-simplified-kotlin]
    // The displayed name should be lowercase with the first letter uppercase, see Resolvers#toHumanName
    @Resolver
    fun getTimeUnitResolverSimplified() = enumResolver<TimeUnit>(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)
}
// --8<-- [end:time_unit_resolver-simplified-kotlin]

@WikiLanguage(WikiLanguage.Language.KOTLIN)
@WikiDetailProfile(WikiDetailProfile.Profile.DETAILED)
// --8<-- [start:time_unit_resolver-detailed-kotlin]
@Resolver
object TimeUnitResolver :
    ClassParameterResolver<TimeUnitResolver, TimeUnit>(TimeUnit::class),
    SlashParameterResolver<TimeUnitResolver, TimeUnit> {

    override val optionType: OptionType = OptionType.STRING

    // This is all you need to implement to support predefined choices
    override fun getPredefinedChoices(guild: Guild?): Collection<Choice> {
        return listOf(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)
            // The Resolvers class helps us by providing resolvers for any enum type.
            // We're just using the helper method to change an enum value to a more natural name.
            .map { Choice(it.toHumanName(), it.name) }
    }

    override suspend fun resolveSuspend(
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): TimeUnit = enumValueOf<TimeUnit>(optionMapping.asString)
}
// --8<-- [end:time_unit_resolver-detailed-kotlin]

fun TimeUnit.localize(time: Long, localizationContext: LocalizationContext): String {
    return localizationContext.switchBundle("Misc")
        .localizeOrNull("time_unit.$name", "time" to time)
        ?: name.lowercase().trimEnd('s')
}