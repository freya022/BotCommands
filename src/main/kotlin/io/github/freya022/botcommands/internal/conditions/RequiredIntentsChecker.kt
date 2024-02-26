package io.github.freya022.botcommands.internal.conditions

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.conditions.RequiredIntents
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName

internal object RequiredIntentsChecker : CustomConditionChecker<RequiredIntents> {
    override val annotationType: Class<RequiredIntents> = RequiredIntents::class.java

    override fun checkServiceAvailability(
        context: BContext,
        checkedClass: Class<*>,
        annotation: RequiredIntents
    ): String? {
        val jdaService = context.getService<JDAService>()
        val missingIntents = annotation.intents.asList() - jdaService.intents
        if (missingIntents.isNotEmpty()) {
            return "${checkedClass.simpleNestedName} requires missing intents: $missingIntents"
        }

        return null
    }
}