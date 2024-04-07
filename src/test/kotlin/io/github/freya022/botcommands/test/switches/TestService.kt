package io.github.freya022.botcommands.test.switches

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.context.annotation.Condition as SpringCondition

object TestServiceChecker : CustomConditionChecker<TestService>, SpringCondition {
    const val useTestServices = true

    override val annotationType: Class<TestService> = TestService::class.java

    override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>, annotation: TestService): String? {
        if (!useTestServices) {
            return "Test services are disabled"
        }

        return null
    }

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return useTestServices;
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(TestServiceChecker::class, fail = false)
@Conditional(TestServiceChecker::class)
annotation class TestService
