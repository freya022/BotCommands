package io.github.freya022.bot.resolvers

import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.localization.context.LocalizationContext
import com.freya02.botcommands.api.localization.to
import com.freya02.botcommands.api.parameters.enumResolver
import java.util.concurrent.TimeUnit

@Suppress("unused")
object TimeUnitResolver {
    // The displayed name should be lowercase with the first letter uppercase, see Resolvers#toHumanName
    @Resolver
    fun get() = enumResolver<TimeUnit>(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)
}

fun TimeUnit.localize(time: Long, localizationContext: LocalizationContext): String {
    return localizationContext.switchBundle("Misc")
        .localizeOrNull("time_unit.$name", "time" to time)
        ?: name.lowercase().trimEnd('s')
}