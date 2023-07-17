package com.freya02.botcommands.api.commands.annotations

import com.freya02.botcommands.api.commands.CooldownScope

import java.util.concurrent.TimeUnit

/**
 * Specifies the cooldown of this text / application command.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cooldown(
    /**
     * Cooldown time [in the specified unit][unit] before the command can be used again in the scope specified by [cooldownScope].
     *
     * @return Cooldown time [in the specified unit][unit]
     */
    val cooldown: Long = 0,

    /**
     * The time unit of the cooldown
     */
    val unit: TimeUnit = TimeUnit.MILLISECONDS,

    /**
     * Scope of the cooldown, either [CooldownScope.USER], [CooldownScope.CHANNEL] or [CooldownScope.GUILD]
     */
    val cooldownScope: CooldownScope = CooldownScope.USER
)
