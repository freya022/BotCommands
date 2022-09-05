package com.freya02.botcommands.internal.commands.autobuilder

import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.application.annotations.NSFW
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.utils.AnnotationUtils
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

fun String.nullIfEmpty(): String? = when {
    isEmpty() -> null
    else -> this
}

//This is used so commands can't prevent other commands from being registered when an exception happens
inline fun <T> Iterable<T>.forEachWithDelayedExceptions(block: (T) -> Unit) {
    var ex: Throwable? = null
    forEach {
        runCatching {
            block(it)
        }.onFailure {
            when (ex) {
                null -> ex = it
                else -> ex!!.addSuppressed(it)
            }
        }
    }

    if (ex != null) {
        throw RuntimeException("Exception(s) occurred while registering annotated commands", ex)
    }
}

fun CommandBuilder.fillCommandBuilder(func: KFunction<*>) {
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

    @Suppress("UNCHECKED_CAST")
    function = func as KFunction<Any>
}

fun ApplicationCommandBuilder.fillApplicationCommandBuilder(func: KFunction<*>) {
    testOnly = AnnotationUtils.getEffectiveTestState(func)
}