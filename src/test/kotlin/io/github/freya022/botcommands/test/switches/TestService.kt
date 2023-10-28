package io.github.freya022.botcommands.test.switches

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.annotations.Condition

object TestServiceChecker : CustomConditionChecker<TestService> {
    const val useTestServices = false

    override val annotationType: Class<TestService> = TestService::class.java

    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>, annotation: TestService): String? {
        if (!useTestServices) {
            return "Test services are disabled"
        }

        return null
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(TestServiceChecker::class, fail = false)
annotation class TestService
