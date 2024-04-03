package io.github.freya022.botcommands.internal.conditions

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.conditions.RequiredIntents
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName

internal object RequiredIntentsChecker : CustomConditionChecker<RequiredIntents> {
    override val annotationType: Class<RequiredIntents> = RequiredIntents::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequiredIntents
    ): String? {
        val jdaService = serviceContainer.getService<JDAService>()
        val missingIntents = annotation.intents.asList() - jdaService.intents
        if (missingIntents.isNotEmpty()) {
            return "${checkedClass.simpleNestedName} requires missing intents: $missingIntents"
        }

        return null
    }
}