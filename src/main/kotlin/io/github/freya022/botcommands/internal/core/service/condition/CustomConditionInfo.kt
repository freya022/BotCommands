package io.github.freya022.botcommands.internal.core.service.condition

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.findAnnotations

internal class CustomConditionInfo(
    internal val checker: CustomConditionChecker<Annotation>,
    internal val conditionMetadata: Condition
) {
    internal fun getCondition(element: KAnnotatedElement): Annotation? =
        element.findAnnotations(checker.annotationType.kotlin).firstOrNull()
}