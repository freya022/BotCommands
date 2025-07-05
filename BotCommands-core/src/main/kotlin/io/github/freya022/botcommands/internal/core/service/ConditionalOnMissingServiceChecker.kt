package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalOnMissingService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName

internal object ConditionalOnMissingServiceChecker : CustomConditionChecker<ConditionalOnMissingService> {

    override val annotationType: Class<ConditionalOnMissingService> = ConditionalOnMissingService::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: ConditionalOnMissingService,
    ): String? {
        val typesToBeAbsent = annotation.value
        typesToBeAbsent.forEach { typeToBeAbsent ->
            // Does not contain the service being checked
            val availableTypes = serviceContainer.getInterfacedServiceTypes(typeToBeAbsent)
            if (availableTypes.isNotEmpty())
                return "An user supplied ${typeToBeAbsent.simpleNestedName} is already active (${availableTypes.first().simpleNestedName})"
        }

        return null
    }
}
