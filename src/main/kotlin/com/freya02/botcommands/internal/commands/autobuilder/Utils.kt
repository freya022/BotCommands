package com.freya02.botcommands.internal.commands.autobuilder

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.IApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.NSFW
import com.freya02.botcommands.api.commands.application.annotations.Test
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.builder.CommandBuilder
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.autobuilder.metadata.CommandFunctionMetadata
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.AnnotationUtils
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

fun String.nullIfEmpty(): String? = when {
    isEmpty() -> null
    else -> this
}

//This is used so commands can't prevent other commands from being registered when an exception happens
internal inline fun <T : CommandFunctionMetadata<*, *>> Iterable<T>.forEachWithDelayedExceptions(block: (T) -> Unit) {
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

internal fun checkCommandId(manager: IApplicationCommandManager, instance: ApplicationCommand, commandId: String, path: CommandPath): Boolean {
    if (manager is GuildApplicationCommandManager) {
        val guildIds = instance.getGuildsForCommandId(commandId, path) ?: return true

        if (manager.guild.idLong !in guildIds) {
            return false //Don't push command if it isn't allowed
        }
    }

    return true
}

internal fun checkTestCommand(manager: IApplicationCommandManager, func: KFunction<*>, scope: CommandScope, context: BContextImpl): Boolean {
    if (func.hasAnnotation<Test>()) {
        if (scope != CommandScope.GUILD) throwUser(func, "Test commands must have their scope set to GUILD")
        if (manager !is GuildApplicationCommandManager) throwInternal("GUILD scoped command was not registered with a guild command manager")

        //Returns whether the command can be registered
        return manager.guild.idLong in AnnotationUtils.getEffectiveTestGuildIds(context, func)
    }

    return true
}

internal fun CommandBuilder.fillCommandBuilder(func: KFunction<*>) {
    func.findAnnotation<Cooldown>()?.let { cooldownAnnotation ->
        cooldown {
            scope = cooldownAnnotation.cooldownScope
            cooldown = cooldownAnnotation.cooldown
            unit = cooldownAnnotation.unit
        }
    }

    userPermissions = AnnotationUtils.getUserPermissions(func)
    botPermissions = AnnotationUtils.getBotPermissions(func)
}

internal fun IBuilderFunctionHolder<in Any>.addFunction(func: KFunction<*>) {
    @Suppress("UNCHECKED_CAST")
    function = func as KFunction<Any>
}

internal fun ApplicationCommandBuilder.fillApplicationCommandBuilder(func: KFunction<*>, annotation: Annotation) {
    if (func.hasAnnotation<NSFW>()) {
        throwUser(func, "@${NSFW::class.simpleName} can only be used on text commands, use the #nsfw method on your annotation instead")
    }

    nsfw = AnnotationUtils.getAnnotationValue(annotation, "nsfw")
}

internal inline fun <reified R> ClassPathFunction.asCommandInstance(): R =
    instance as? R ?: throwUser(function, "Declaring class must extend ${R::class.simpleName}")