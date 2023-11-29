package io.github.freya022.wiki.switches

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.annotations.Condition

// Singleton which checks if a service annotated with @WikiLanguage can be instantiated
object WikiLanguageChecker : CustomConditionChecker<WikiLanguage> {
    // NOTE: When changing wiki source code, the wiki downloads snippets at build time,
    // and must be rebuilt for the changes to be taken into account
    private val currentLanguage = WikiLanguage.Language.KOTLIN

    override val annotationType: Class<WikiLanguage> = WikiLanguage::class.java

    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>, annotation: WikiLanguage): String? {
        val serviceLanguage = annotation.language
        if (serviceLanguage == currentLanguage) {
            return null
        }

        return "Invalid language, current language: $currentLanguage, service language: $serviceLanguage"
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
// Custom annotation which acts like a conditional service annotation,
// The checks are made with WikiLanguageChecker
@Condition(WikiLanguageChecker::class, fail = false)
annotation class WikiLanguage(@get:JvmName("value") val language: Language) {
    enum class Language {
        JAVA,
        KOTLIN
    }
}
