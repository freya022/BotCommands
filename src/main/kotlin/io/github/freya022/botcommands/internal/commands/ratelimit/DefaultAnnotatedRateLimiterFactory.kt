package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.AnnotatedRateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.getInterfacedServiceTypes
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.classRef
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@BService
internal open class DefaultAnnotatedRateLimiterFactoryProvider {
    @Bean
    @BService
    @ConditionalService(ExistingSupplierChecker::class)
    @ConditionalOnMissingBean(AnnotatedRateLimiterFactory::class)
    open fun defaultAnnotatedRateLimiterFactory(): AnnotatedRateLimiterFactory = object : AnnotatedRateLimiterFactory {
        override fun create(scope: RateLimitScope, configurationSupplier: BucketConfigurationSupplier, deleteOnRefill: Boolean): RateLimiter =
            DefaultRateLimiter(scope, configurationSupplier, deleteOnRefill)
    }

    internal object ExistingSupplierChecker : ConditionalServiceChecker {
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String? {
            val suppliers = serviceContainer.getInterfacedServiceTypes<AnnotatedRateLimiterFactory>()
            if (suppliers.isNotEmpty())
                return "An user supplied ${classRef<AnnotatedRateLimiterFactory>()} is already active (${suppliers.first().simpleNestedName})"

            return null
        }
    }
}