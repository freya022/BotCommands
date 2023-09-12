package io.github.freya022.bot.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.api.localization.LocalizationService
import com.freya02.botcommands.api.localization.context.LocalizationContext
import com.freya02.botcommands.api.localization.to
import com.freya02.botcommands.api.parameters.enumResolver
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("unused")
object TimeUnitResolver {
    // The displayed name should be lowercase with the first letter uppercase, see Resolvers#toHumanName
    @Resolver
    fun get() = enumResolver<TimeUnit>(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)
}

fun TimeUnit.localize(time: Long, context: BContext, localizationContext: LocalizationContext): String {
    //TODO replace with withBundle + localizeOrNull
    val localization = context
        .getService<LocalizationService>()
        .getInstance("Misc", localizationContext.effectiveLocale.let { Locale.forLanguageTag(it.locale) })
        ?: throw IllegalStateException("Unable to find the 'Misc' localization file")
    return localization["time_unit.$name"]?.localize("time" to time) ?: name.lowercase().trimEnd('s')
}