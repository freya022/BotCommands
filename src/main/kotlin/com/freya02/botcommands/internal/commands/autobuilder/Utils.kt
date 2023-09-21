package com.freya02.botcommands.internal.commands.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.*
import com.freya02.botcommands.api.commands.application.AbstractApplicationCommandManager
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.Test
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.builder.CommandBuilder
import com.freya02.botcommands.api.commands.prefixed.annotations.NSFW
import com.freya02.botcommands.api.commands.ratelimit.RateLimiter
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.api.parameters.ICustomResolver
import com.freya02.botcommands.api.parameters.ParameterWrapper.Companion.wrap
import com.freya02.botcommands.api.parameters.ResolverContainer
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.autobuilder.metadata.CommandFunctionMetadata
import com.freya02.botcommands.internal.utils.AnnotationUtils
import com.freya02.botcommands.internal.utils.ReflectionUtils.declaringClass
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import com.freya02.botcommands.internal.utils.requireUser
import com.freya02.botcommands.internal.utils.throwInternal
import com.freya02.botcommands.internal.utils.throwUser
import java.time.Duration
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import io.github.bucket4j.Bandwidth as BucketBandwidth
import io.github.bucket4j.Refill as BucketRefill

//This is used so commands can't prevent other commands from being registered when an exception happens
internal inline fun <T : CommandFunctionMetadata<*, *>> Iterable<T>.forEachWithDelayedExceptions(crossinline block: (T) -> Unit) {
    var ex: Throwable? = null
    forEach { metadata ->
        runCatching {
            block(metadata)
        }.onFailure {
            when (ex) {
                null -> ex = it.addFunction(metadata)
                else -> ex!!.addSuppressed(it.addFunction(metadata))
            }
        }
    }

    if (ex != null) {
        throw RuntimeException("Exception(s) occurred while registering annotated commands", ex)
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun <T : CommandFunctionMetadata<*, *>> Throwable.addFunction(metadata: T) =
    RuntimeException("An exception occurred while processing function ${metadata.func.shortSignature}", this)

internal fun checkCommandId(manager: AbstractApplicationCommandManager, instance: ApplicationCommand, commandId: String, path: CommandPath): Boolean {
    if (manager is GuildApplicationCommandManager) {
        val guildIds = instance.getGuildsForCommandId(commandId, path) ?: return true

        if (manager.guild.idLong !in guildIds) {
            return false //Don't push command if it isn't allowed
        }
    }

    return true
}

internal enum class TestState {
    INCLUDE,
    EXCLUDE,
    NO_ANNOTATION
}

internal fun checkTestCommand(manager: AbstractApplicationCommandManager, func: KFunction<*>, scope: CommandScope, context: BContextImpl): TestState {
    if (func.hasAnnotation<Test>()) {
        if (scope != CommandScope.GUILD) throwUser(func, "Test commands must have their scope set to GUILD")
        if (manager !is GuildApplicationCommandManager) throwInternal("GUILD scoped command was not registered with a guild command manager")

        //Returns whether the command can be registered
        return when (manager.guild.idLong) {
            in AnnotationUtils.getEffectiveTestGuildIds(context, func) -> TestState.INCLUDE
            else -> TestState.EXCLUDE
        }
    }

    return TestState.NO_ANNOTATION
}

internal fun CommandBuilder.fillCommandBuilder(func: KFunction<*>) {
    val rateLimitAnnotation = func.findAnnotation<RateLimit>() ?: func.declaringClass.findAnnotation<RateLimit>()
    val cooldownAnnotation = func.findAnnotation<Cooldown>() ?: func.declaringClass.findAnnotation<Cooldown>()
    requireUser(cooldownAnnotation == null || rateLimitAnnotation == null, func) {
        "Cannot use both @${Cooldown::class.simpleNestedName} and @${RateLimit::class.simpleNestedName}"
    }

    if (rateLimitAnnotation != null) {
        fun Refill.toRealRefill(): BucketRefill {
            val duration = Duration.of(period, periodUnit)
            return when (type) {
                RefillType.GREEDY -> BucketRefill.greedy(tokens, duration)
                RefillType.INTERVAL -> BucketRefill.intervally(tokens, duration)
            }
        }

        fun Bandwidth.toRealBandwidth(): BucketBandwidth {
            return BucketBandwidth.classic(capacity, refill.toRealRefill())
        }

        rateLimit(BucketFactory.custom(rateLimitAnnotation.baseBandwidth.toRealBandwidth(), rateLimitAnnotation.spikeBandwidth.toRealBandwidth()))
    }

    if (cooldownAnnotation != null) {
        rateLimit(BucketFactory.ofCooldown(Duration.of(cooldownAnnotation.cooldown, cooldownAnnotation.unit)), RateLimiter.defaultFactory(cooldownAnnotation.rateLimitScope))
    }

    userPermissions = AnnotationUtils.getUserPermissions(func)
    botPermissions = AnnotationUtils.getBotPermissions(func)
}

@Suppress("UNCHECKED_CAST")
internal fun KFunction<*>.castFunction() = this as KFunction<Any>

internal fun ApplicationCommandBuilder<*>.fillApplicationCommandBuilder(func: KFunction<*>, annotation: Annotation) {
    if (func.hasAnnotation<NSFW>()) {
        throwUser(func, "@${NSFW::class.simpleName} can only be used on text commands, use the #nsfw method on your annotation instead")
    }

    nsfw = AnnotationUtils.getAnnotationValue(annotation, "nsfw")
}

internal fun ResolverContainer.requireCustomOption(func: KFunction<*>, kParameter: KParameter, optionAnnotation: KClass<out Annotation>) {
    val parameterWrapper = kParameter.wrap()
    if (getResolver(parameterWrapper) !is ICustomResolver<*, *>) {
        throwUser(func, "Custom option '${parameterWrapper.name}' (${parameterWrapper.type.simpleNestedName}) does not have a compatible ICustomResolver, " +
                "if this is a Discord option, use @${optionAnnotation.simpleNestedName}")
    }
}