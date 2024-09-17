package io.github.freya022.botcommands.internal.core.service.condition

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.core.service.provider.ServiceProvider
import io.github.freya022.botcommands.internal.core.service.provider.findAnnotation

internal class CustomConditionInfo(
    internal val checker: CustomConditionChecker<Annotation>,
    internal val conditionMetadata: Condition
) {
    internal fun getCondition(serviceProvider: ServiceProvider): Annotation? =
        serviceProvider.findAnnotation(checker.annotationType.kotlin)
}