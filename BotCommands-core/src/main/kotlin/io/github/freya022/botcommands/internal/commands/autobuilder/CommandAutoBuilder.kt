package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.bucket4j.Bandwidth as BucketBandwidth
import io.github.freya022.botcommands.api.commands.annotations.Bandwidth as BandwidthAnnotation
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.annotations.RefillType
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.bestName
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.ratelimit.AnnotatedRateLimiterFactory
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.ratelimit.bucket.toSupplier
import io.github.freya022.botcommands.internal.commands.autobuilder.utils.ParameterAdapter
import io.github.freya022.botcommands.internal.core.service.canCreateWrappedService
import io.github.freya022.botcommands.internal.utils.AnnotationUtils
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwArgument
import java.time.Duration
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

abstract class CommandAutoBuilder {
    protected abstract val serviceContainer: ServiceContainer
    protected abstract val optionAnnotation: KClass<out Annotation>

    protected fun CommandBuilder.fillCommandBuilder(functions: List<KFunction<*>>) {
        declarationSite = functions.first().let(DeclarationSite::fromFunctionSignature)

        val rateLimiter = functions.singleValueOfVariants("their rate limit specification", ::readRateLimit)
        val rateLimitRef = functions.singleAnnotationOfVariants<RateLimitReference>()

        // A single one of them can be used - One of them needs to be null
        check(rateLimitRef == null || rateLimiter == null) {
            "You can either define a rate limit or reference one, but not both"
        }

        if (rateLimiter != null) {
            rateLimit(rateLimiter) {
                declarationSite = this@fillCommandBuilder.declarationSite
            }
        }

        if (rateLimitRef != null) {
            rateLimitReference(rateLimitRef.group)
        }

        functions
            .singleValueOfVariants("user permission") { f ->
                AnnotationUtils.getUserPermissions(f).takeIf { it.isNotEmpty() }
            }
            ?.let { userPermissions = it }
        functions
            .singleValueOfVariants("bot permissions") { f ->
                AnnotationUtils.getBotPermissions(f).takeIf { it.isNotEmpty() }
            }
            ?.let { botPermissions = it }
    }

    protected fun CommandBuilder.fillCommandBuilder(func: KFunction<*>) = fillCommandBuilder(listOf(func))

    private fun readRateLimit(func: KFunction<*>): RateLimiter? {
        val rateLimitAnnotation = func.findAnnotationRecursive<RateLimit>() ?: func.declaringClass.findAnnotationRecursive<RateLimit>()
        val cooldownAnnotation = func.findAnnotationRecursive<Cooldown>() ?: func.declaringClass.findAnnotationRecursive<Cooldown>()
        requireAt(cooldownAnnotation == null || rateLimitAnnotation == null, func) {
            "Cannot use both ${annotationRef<Cooldown>()} and ${annotationRef<RateLimit>()}"
        }

        return if (rateLimitAnnotation != null) {
            val bucketConfigurationSupplier = Buckets.custom(rateLimitAnnotation.bandwidths.map { it.toRealBandwidth() }).toSupplier()
            val annotatedRateLimiterFactory = serviceContainer.getService<AnnotatedRateLimiterFactory>()
            annotatedRateLimiterFactory.create(rateLimitAnnotation.scope, bucketConfigurationSupplier, rateLimitAnnotation.deleteOnRefill)
        } else if (cooldownAnnotation != null) {
            val bucketConfigurationSupplier = Buckets.ofCooldown(Duration.of(cooldownAnnotation.cooldown, cooldownAnnotation.unit)).toSupplier()
            val annotatedRateLimiterFactory = serviceContainer.getService<AnnotatedRateLimiterFactory>()
            annotatedRateLimiterFactory.create(cooldownAnnotation.scope, bucketConfigurationSupplier, cooldownAnnotation.deleteOnRefill)
        } else {
            null
        }
    }

    private fun BandwidthAnnotation.toRealBandwidth(): BucketBandwidth =
        BucketBandwidth.builder()
            .capacity(capacity)
            .let {
                val duration = Duration.of(refill.period, refill.periodUnit)
                when (refill.type) {
                    RefillType.GREEDY -> it.refillGreedy(refill.tokens, duration)
                    RefillType.INTERVAL -> it.refillIntervally(refill.tokens, duration)
                }
            }
            .build()

    protected fun requireServiceOptionOrOptional(func: KFunction<*>, parameterAdapter: ParameterAdapter, commandAnnotation: KClass<out Annotation>) {
        if (parameterAdapter.isOptionalOrNullable) return

        val serviceError = serviceContainer.canCreateWrappedService(parameterAdapter.valueParameter) ?: return
        val originalParameter = parameterAdapter.originalParameter
        throwArgument(
            func,
            "Cannot determine usage of option '${originalParameter.bestName}' (${originalParameter.type.simpleNestedName}) and service loading failed, " +
                    "if this is a Discord option, use @${optionAnnotation.simpleNestedName}, check @${commandAnnotation.simpleNestedName} for more details\n" +
                    serviceError.toDetailedString()
        )
    }

    protected inline fun <reified E : Enum<E>> Array<out E>.toEnumSetOr(fallback: Set<E>): Set<E> = when {
        this.isEmpty() -> fallback
        else -> enumSetOf<E>(*this)
    }
}
