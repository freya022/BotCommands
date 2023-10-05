package io.github.freya022.bot.switches

import io.github.freya022.botcommands.api.BContext
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker

annotation class SimpleFrontend

object FrontendChooser : ConditionalServiceChecker {
    private const val USE_SIMPLIFIED_FRONT = true

    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>) = when {
        USE_SIMPLIFIED_FRONT && !checkedClass.isAnnotationPresent(SimpleFrontend::class.java) -> "This frontend was disabled"
        else -> null
    }
}