package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.service.CustomConditionChecker
import com.freya02.botcommands.api.core.service.annotations.Condition
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.findAnnotations

internal class CustomConditionInfo(
    internal val checker: CustomConditionChecker<Annotation>,
    internal val conditionMetadata: Condition
) {
    internal fun getCondition(element: KAnnotatedElement): Annotation? =
        element.findAnnotations(checker.annotationType.kotlin).firstOrNull()
}