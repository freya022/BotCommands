package io.github.freya022.bot.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.context.LocalizationContext
import io.github.freya022.botcommands.api.localization.to
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverManager
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverProvider
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.withSlashCommands
import io.github.freya022.botcommands.api.parameters.resolvers.registerEnum
import java.util.concurrent.TimeUnit

// Make "TimeUnit" parseable
@BService
object TimeUnitResolverProvider : ResolverProvider {
    override fun declare(manager: ResolverManager) {
        manager.registerEnum<TimeUnit> {
            // Override values
            setValues(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)

            // Enable support for slash commands
            withSlashCommands()
        }
    }
}

fun TimeUnit.localize(time: Long, localizationContext: LocalizationContext): String {
    return localizationContext.switchBundle("Misc")
        .localizeOrNull("time_unit.$name", "time" to time)
        ?: name.lowercase().trimEnd('s')
}
