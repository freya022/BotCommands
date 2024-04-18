package io.github.freya022.botcommands.test.switches

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.context.annotation.Condition as SpringCondition

object TestLanguageChecker : CustomConditionChecker<TestLanguage>, SpringCondition {
    private val currentLanguage = TestLanguage.Language.KOTLIN

    override val annotationType: Class<TestLanguage> = TestLanguage::class.java

    override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>, annotation: TestLanguage): String? {
        val serviceLanguage = annotation.language
        if (serviceLanguage == currentLanguage) {
            return null
        }

        return "Invalid language, current language: $currentLanguage, service language: $serviceLanguage"
    }

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return currentLanguage == metadata.annotations
                .get(TestLanguage::class.java)
                .synthesize().language
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(TestLanguageChecker::class, fail = false)
@Conditional(TestLanguageChecker::class)
annotation class TestLanguage(@get:JvmName("value") val language: Language) {
    enum class Language {
        JAVA,
        KOTLIN
    }
}
