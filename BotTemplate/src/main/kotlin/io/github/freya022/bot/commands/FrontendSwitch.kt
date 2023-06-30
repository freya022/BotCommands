package io.github.freya022.bot.commands

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.ConditionalServiceChecker

annotation class SimpleFrontend

object FrontendChooser : ConditionalServiceChecker {
    private const val useSimplifiedFront = true

    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>) = when {
        useSimplifiedFront && !checkedClass.isAnnotationPresent(SimpleFrontend::class.java) -> "This frontend was disabled"
        else -> null
    }
}