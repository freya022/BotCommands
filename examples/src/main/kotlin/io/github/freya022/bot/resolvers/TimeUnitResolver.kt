package io.github.freya022.bot.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import io.github.freya022.botcommands.api.localization.to
import io.github.freya022.botcommands.api.parameters.enumResolver
import java.util.concurrent.TimeUnit

@Suppress("unused")
object TimeUnitResolver {
    // The displayed name should be lowercase with the first letter uppercase, see Resolvers#toHumanName
    @Resolver
    fun getTimeUnitResolverSimplified() = enumResolver<TimeUnit>(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)
}

fun TimeUnit.localize(time: Long, localizationContext: LocalizationContext): String {
    return localizationContext.switchBundle("Misc")
        .localizeOrNull("time_unit.$name", "time" to time)
        ?: name.lowercase().trimEnd('s')
}