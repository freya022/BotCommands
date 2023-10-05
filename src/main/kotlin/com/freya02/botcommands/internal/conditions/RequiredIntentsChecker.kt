package com.freya02.botcommands.internal.conditions

import com.freya02.botcommands.api.core.BContext
import com.freya02.botcommands.api.core.JDAService
import com.freya02.botcommands.api.core.conditions.RequiredIntents
import com.freya02.botcommands.api.core.service.CustomConditionChecker
import com.freya02.botcommands.api.core.service.getServiceOrNull
import com.freya02.botcommands.api.core.utils.simpleNestedName

internal object RequiredIntentsChecker : CustomConditionChecker<RequiredIntents> {
    override val annotationType: Class<RequiredIntents> = RequiredIntents::class.java

    override fun checkServiceAvailability(
        context: BContext,
        checkedClass: Class<*>,
        annotation: RequiredIntents
    ): String? {
        context.getServiceOrNull<JDAService>()?.let { jdaService ->
            val missingIntents = annotation.intents.asList() - jdaService.intents
            if (missingIntents.isNotEmpty()) {
                return "${checkedClass.simpleNestedName} requires missing intents: $missingIntents"
            }
        }

        return null
    }
}