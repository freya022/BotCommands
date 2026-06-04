package io.github.freya022.botcommands.internal.commands.application.autobuilder

import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.findAllAnnotations
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.flatMapTo
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.annotations.Cooldown
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimit
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimitReference
import io.github.freya022.botcommands.internal.commands.application.autobuilder.metadata.ApplicationFunctionMetadata
import io.github.freya022.botcommands.internal.commands.autobuilder.CommandUnit
import io.github.freya022.botcommands.internal.commands.autobuilder.RateLimitAutoBuilderHelper
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.Permission
import java.util.EnumSet

internal class ApplicationCommandUnit(
    private val serviceContainer: ServiceContainer,
    private val cmdMetadata: ApplicationFunctionMetadata<*>,
) : CommandUnit {

    override fun getBestDeclarationSite(): DeclarationSite {
        return DeclarationSite.fromFunctionSignature(cmdMetadata.func)
    }

    context(builder: CommandBuilder)
    override fun getRateLimiter(): RateLimiter? {
        val rateLimitAnnotation = cmdMetadata.func.findAnnotationRecursive<RateLimit>() ?: cmdMetadata.declaringClass.findAnnotationRecursive<RateLimit>()
        val cooldownAnnotation = cmdMetadata.func.findAnnotationRecursive<Cooldown>() ?: cmdMetadata.declaringClass.findAnnotationRecursive<Cooldown>()

        return when {
            cooldownAnnotation == null && rateLimitAnnotation == null -> null
            cooldownAnnotation != null && rateLimitAnnotation != null ->
                throwArgument(cmdMetadata.func, "Cannot use both ${annotationRef<Cooldown>()} and ${annotationRef<RateLimit>()}")
            rateLimitAnnotation != null ->
                RateLimitAutoBuilderHelper.readRateLimit(serviceContainer, rateLimitAnnotation)
            cooldownAnnotation != null ->
                RateLimitAutoBuilderHelper.readCooldown(serviceContainer, cooldownAnnotation)
            else -> throwInternal("Should have been exhaustive")
        }
    }

    context(builder: CommandBuilder)
    override fun getRateLimitRef(): RateLimitReference? {
        return cmdMetadata.func.findAnnotationRecursive<RateLimitReference>()
            ?: cmdMetadata.declaringClass.findAnnotationRecursive<RateLimitReference>()
    }

    context(builder: CommandBuilder)
    override fun getBotPermissions(): EnumSet<Permission> {
        return cmdMetadata.func.findAllAnnotations<BotPermissions>().flatMapTo(enumSetOf()) { it.permissions }
    }

    context(builder: CommandBuilder)
    override fun getUserPermissions(): EnumSet<Permission> {
        return cmdMetadata.func.findAllAnnotations<UserPermissions>().flatMapTo(enumSetOf()) { it.permissions }
    }
}
