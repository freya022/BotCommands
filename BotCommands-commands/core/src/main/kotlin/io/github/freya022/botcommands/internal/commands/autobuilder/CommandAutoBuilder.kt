package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.utils.bestName
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.autobuilder.utils.ParameterAdapter
import io.github.freya022.botcommands.internal.core.service.canCreateWrappedService
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

abstract class CommandAutoBuilder {
    abstract val serviceContainer: ServiceContainer
    protected abstract val optionAnnotation: KClass<out Annotation>

    protected fun CommandBuilder.fillCommandBuilder(commandUnit: CommandUnit) {
        declarationSite = commandUnit.getBestDeclarationSite()

        val rateLimiter = commandUnit.getRateLimiter()
        val rateLimitRef = commandUnit.getRateLimitRef()

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

        userPermissions = commandUnit.getUserPermissions()
        botPermissions = commandUnit.getBotPermissions()
    }

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
