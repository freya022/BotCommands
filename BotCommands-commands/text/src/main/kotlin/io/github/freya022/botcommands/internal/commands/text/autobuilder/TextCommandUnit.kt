package io.github.freya022.botcommands.internal.commands.text.autobuilder

import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.findAllAnnotations
import io.github.freya022.botcommands.api.core.utils.flatMapTo
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.annotations.Cooldown
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimit
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimitReference
import io.github.freya022.botcommands.internal.commands.autobuilder.CommandUnit
import io.github.freya022.botcommands.internal.commands.autobuilder.RateLimitAutoBuilderHelper
import io.github.freya022.botcommands.internal.commands.text.autobuilder.utils.TextCommandVariationContainer
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.Permission
import java.util.EnumSet

internal class TextCommandUnit(
    private val serviceContainer: ServiceContainer,
    private val variations: TextCommandVariationContainer,
) : CommandUnit {

    override fun getBestDeclarationSite(): DeclarationSite {
        return DeclarationSite.fromFunctionSignature(variations.asList.first().func)
    }

    context(builder: CommandBuilder)
    override fun getRateLimiter(): RateLimiter? {
        val rateLimitAnnotation = variations.singleAnnotationOfVariants<RateLimit>()
        val cooldownAnnotation = variations.singleAnnotationOfVariants<Cooldown>()

        return when {
            cooldownAnnotation == null && rateLimitAnnotation == null -> null
            cooldownAnnotation != null && rateLimitAnnotation != null ->
                throwArgument("Cannot use both ${annotationRef<Cooldown>()} and ${annotationRef<RateLimit>()} on text command '${builder.path}")
            rateLimitAnnotation != null ->
                RateLimitAutoBuilderHelper.readRateLimit(serviceContainer, rateLimitAnnotation)
            cooldownAnnotation != null ->
                RateLimitAutoBuilderHelper.readCooldown(serviceContainer, cooldownAnnotation)
            else -> throwInternal("Should have been exhaustive")
        }
    }

    context(builder: CommandBuilder)
    override fun getRateLimitRef(): RateLimitReference? {
        return variations.singleAnnotationOfVariants<RateLimitReference>()
    }

    context(builder: CommandBuilder)
    override fun getBotPermissions(): EnumSet<Permission> {
        return variations
            .singleValueOfVariants(annotationRef<BotPermissions>()) { f ->
                f.findAllAnnotations<BotPermissions>().flatMapTo(enumSetOf()) { it.permissions }
                    .takeIf { it.isNotEmpty() }
            }
            ?: enumSetOf()
    }

    context(builder: CommandBuilder)
    override fun getUserPermissions(): EnumSet<Permission> {
        return variations
            .singleValueOfVariants(annotationRef<UserPermissions>()) { f ->
                f.findAllAnnotations<UserPermissions>().flatMapTo(enumSetOf()) { it.permissions }
                    .takeIf { it.isNotEmpty() }
            }
            ?: enumSetOf()
    }
}
