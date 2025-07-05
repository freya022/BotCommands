package io.github.freya022.botcommands.internal.core.conditions

import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.conditions.RequiredIntents
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.env.getProperty
import org.springframework.core.type.AnnotatedTypeMetadata

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

internal object SpringRequiredIntentsChecker : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val currentIntents = context.environment
            .getProperty<List<String>>("jda.intents")
            ?.mapTo(enumSetOf<GatewayIntent>(), GatewayIntent::valueOf)
            ?: JDAService.defaultIntents
        val requiredIntents = metadata.annotations.get(RequiredIntentsChecker.annotationType).getEnumArray("value", GatewayIntent::class.java).toSet()
        val missingIntents = requiredIntents - currentIntents
        return missingIntents.isEmpty()
    }
}