package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.service.CustomConditionChecker
import com.freya02.botcommands.api.core.service.annotations.Condition
import java.lang.reflect.AnnotatedElement

internal class CustomConditionInfo(
    internal val checker: CustomConditionChecker<Annotation>,
    internal val conditionMetadata: Condition
) {
    internal fun getCondition(element: AnnotatedElement): Annotation? =
        element.getDeclaredAnnotation(checker.annotationType)
}