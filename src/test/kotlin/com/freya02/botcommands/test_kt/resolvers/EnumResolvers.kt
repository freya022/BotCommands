package com.freya02.botcommands.test_kt.resolvers

import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.parameters.enumResolver
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Suppress("unused")
object EnumResolvers {
    @Resolver
    fun timeUnitResolver() = enumResolver<TimeUnit>(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)

    @Resolver
    fun chronoUnitResolver() = enumResolver<ChronoUnit>(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES)
}