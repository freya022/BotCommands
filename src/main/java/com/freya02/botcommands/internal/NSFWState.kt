package com.freya02.botcommands.internal

import com.freya02.botcommands.annotations.api.annotations.NSFW
import com.freya02.botcommands.internal.utils.AnnotationUtils.getEffectiveAnnotation
import kotlin.reflect.KFunction

class NSFWState(val isEnabledInGuild: Boolean, val isEnabledInDMs: Boolean) {
    init {
        require(isEnabledInDMs || isEnabledInGuild) { "Cannot disable both guild and DMs NSFW, as it would disable the command permanently" }
    }

    companion object {
        fun ofMethod(function: KFunction<*>): NSFWState? {
            val nsfw: NSFW = getEffectiveAnnotation(function, NSFW::class) ?: return null
            return NSFWState(nsfw.guild, nsfw.dm)
        }
    }
}