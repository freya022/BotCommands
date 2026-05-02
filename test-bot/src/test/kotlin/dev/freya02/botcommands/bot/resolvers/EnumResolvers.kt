package dev.freya02.botcommands.bot.resolvers

import io.github.freya022.botcommands.api.commands.application.resolvers.enumerations.withSlashCommands
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverManager
import io.github.freya022.botcommands.api.parameters.resolvers.ResolverProvider
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.withComponents
import io.github.freya022.botcommands.api.parameters.resolvers.enumerations.withTextCommands
import io.github.freya022.botcommands.api.parameters.resolvers.registerEnum
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@BService
object TimeUnitResolverProvider : ResolverProvider {
    override fun declare(manager: ResolverManager) {
        manager.registerEnum<TimeUnit> {
            setValues(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)

            withTextCommands()
            withSlashCommands()
            withComponents()
        }

        manager.registerEnum<ChronoUnit> {
            setValues(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES)

            withSlashCommands()
        }
    }
}
