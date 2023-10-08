package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.enumResolver
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Suppress("unused")
object EnumResolvers {
    @Resolver
    fun timeUnitResolver() = enumResolver<TimeUnit>(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)

    @Resolver
    fun chronoUnitResolver() = enumResolver<ChronoUnit>(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES)
}