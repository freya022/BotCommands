package io.github.freya022.bot.switches

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker

// This is just an annotation to switch between command declaration modes
// "Simple frontends" are just annotated commands, without the actual logic on it
// Other frontends include DSL-declared commands

annotation class SimpleFrontend

object FrontendChooser : ConditionalServiceChecker {
    private const val USE_SIMPLIFIED_FRONT = true

    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>) = when {
        USE_SIMPLIFIED_FRONT && !checkedClass.isAnnotationPresent(SimpleFrontend::class.java) -> "This frontend was disabled"
        else -> null
    }
}