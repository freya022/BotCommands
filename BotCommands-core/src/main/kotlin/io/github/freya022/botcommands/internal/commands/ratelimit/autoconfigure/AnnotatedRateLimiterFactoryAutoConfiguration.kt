package io.github.freya022.botcommands.internal.commands.ratelimit.autoconfigure

import io.github.freya022.botcommands.api.commands.ratelimit.AnnotatedRateLimiterFactory
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfiguration
import io.github.freya022.botcommands.internal.core.service.annotations.InternalAutoConfigurationBeanService

@InternalAutoConfiguration
internal open class AnnotatedRateLimiterFactoryAutoConfiguration {

    @InternalAutoConfigurationBeanService
    @ConditionalOnMissingService(AnnotatedRateLimiterFactory::class)
    open fun annotatedRateLimiterFactory(): AnnotatedRateLimiterFactory {
        return DefaultAnnotatedRateLimiterFactory
    }
}
