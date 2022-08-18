package com.freya02.botcommands.commands.internal.autobuilder

import com.freya02.botcommands.annotations.api.annotations.Cooldown
import com.freya02.botcommands.annotations.api.annotations.NSFW
import com.freya02.botcommands.api.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.internal.utils.AnnotationUtils
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

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