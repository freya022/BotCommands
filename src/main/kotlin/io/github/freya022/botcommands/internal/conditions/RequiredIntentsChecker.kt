package io.github.freya022.botcommands.internal.conditions

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.conditions.RequiredIntents
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.getServiceOrNull
import io.github.freya022.botcommands.api.core.utils.simpleNestedName

internal object RequiredIntentsChecker : CustomConditionChecker<RequiredIntents> {
    override val annotationType: Class<RequiredIntents> = RequiredIntents::class.java

    override fun checkServiceAvailability(
        context: BContext,
        checkedClass: Class<*>,
        annotation: RequiredIntents
    ): String? {
        val jdaService = context.getServiceOrNull<JDAService>()
        checkNotNull(jdaService) {
            "A JDAService instance must be present in order to use @${RequiredIntents::class.simpleName}"
        }

        val missingIntents = annotation.intents.asList() - jdaService.intents
        if (missingIntents.isNotEmpty()) {
            return "${checkedClass.simpleNestedName} requires missing intents: $missingIntents"
        }

        return null
    }
}