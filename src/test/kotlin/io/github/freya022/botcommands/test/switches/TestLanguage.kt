package io.github.freya022.botcommands.test.switches

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition

object TestLanguageChecker : CustomConditionChecker<TestLanguage> {
    private val currentLanguage = TestLanguage.Language.KOTLIN

    override val annotationType: Class<TestLanguage> = TestLanguage::class.java

    override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>, annotation: TestLanguage): String? {
        val serviceLanguage = annotation.language
        if (serviceLanguage == currentLanguage) {
            return null
        }

        return "Invalid language, current language: $currentLanguage, service language: $serviceLanguage"
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(TestLanguageChecker::class, fail = false)
annotation class TestLanguage(@get:JvmName("value") val language: Language) {
    enum class Language {
        JAVA,
        KOTLIN
    }
}
