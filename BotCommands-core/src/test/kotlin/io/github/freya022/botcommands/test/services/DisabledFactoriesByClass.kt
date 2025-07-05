package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import net.dv8tion.jda.api.JDA

@BService
@ConditionalService(DisabledFactoriesByClass.Companion::class)
class DisabledFactoriesByClass {
    @BService
    fun jda(): JDA = throw AssertionError()

    companion object : ConditionalServiceChecker {
        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>): String {
            return "Disabled class and factories"
        }
    }
}