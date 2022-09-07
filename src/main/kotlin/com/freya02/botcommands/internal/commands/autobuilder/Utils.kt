package com.freya02.botcommands.internal.commands.autobuilder

import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.application.annotations.NSFW
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.commands.autobuilder.metadata.CommandFunctionMetadata
import com.freya02.botcommands.internal.utils.AnnotationUtils
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

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

fun CommandBuilder.fillCommandBuilder(func: KFunction<*>, putFunction: Boolean) {
    func.findAnnotation<Cooldown>()?.let { cooldownAnnotation ->
        cooldown {
            scope = cooldownAnnotation.cooldownScope
            cooldown = cooldownAnnotation.cooldown
            unit = cooldownAnnotation.unit
        }
    }

    func.findAnnotation<NSFW>()?.let { nsfwAnnotation ->
        nsfw {
            allowInDMs = nsfwAnnotation.dm
            allowInGuild = nsfwAnnotation.guild
        }
    }

    userPermissions = AnnotationUtils.getUserPermissions(func)
    botPermissions = AnnotationUtils.getBotPermissions(func)

    if (putFunction) {
        @Suppress("UNCHECKED_CAST")
        function = func as KFunction<Any>
    }
}

fun ApplicationCommandBuilder.fillApplicationCommandBuilder(func: KFunction<*>) {
    testOnly = AnnotationUtils.getEffectiveTestState(func)
}