package com.freya02.botcommands.test_kt.services.annotations

import com.freya02.botcommands.api.core.service.annotations.Condition
import com.freya02.botcommands.test_kt.services.MyCustomConditionChecker

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(MyCustomConditionChecker::class, fail = false)
annotation class RequiredNumber(val number: Int)
