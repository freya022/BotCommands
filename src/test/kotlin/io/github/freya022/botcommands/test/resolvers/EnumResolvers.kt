package io.github.freya022.botcommands.test.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.enumResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@BService // Cannot be a BConfiguration as the function below needs an instance
@Configuration
class EnumResolvers {
    @Resolver
    @Bean
    fun timeUnitResolver() = enumResolver<TimeUnit>(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)

    @Resolver
    @Bean
    fun chronoUnitResolver() = enumResolver<ChronoUnit>(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES)
}