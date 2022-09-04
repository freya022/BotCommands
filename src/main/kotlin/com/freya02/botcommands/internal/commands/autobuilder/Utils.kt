package com.freya02.botcommands.internal.commands.autobuilder

import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.application.annotations.NSFW
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.utils.AnnotationUtils
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

//This is used so commands can't prevent other commands from being registered when an exception happens
inline fun <T> Iterable<T>.forEachWithDelayedExceptions(block: (T) -> Unit) {
    val exceptions: MutableList<Throwable> = arrayListOf()
    forEach {
        runCatching {
            block(it)
        }.onFailure {
           exceptions.add(it)
        }
    }

    if (exceptions.isNotEmpty()) {
        throw RuntimeException("${exceptions.size} exception(s) occurred while registering annotated commands, here is the first exception:", exceptions.first())
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