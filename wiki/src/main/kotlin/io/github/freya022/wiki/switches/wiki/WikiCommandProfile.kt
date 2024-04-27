package io.github.freya022.wiki.switches.wiki

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition

object WikiCommandProfileChecker : CustomConditionChecker<WikiCommandProfile> {
    // NOTE: When changing wiki source code, the wiki downloads snippets at build time,
    // and must be rebuilt for the changes to be taken into account
    private val currentProfile = WikiCommandProfile.Profile.KOTLIN_DSL

    override val annotationType: Class<WikiCommandProfile> = WikiCommandProfile::class.java

    override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>, annotation: WikiCommandProfile): String? {
        val serviceProfile = annotation.profile
        if (serviceProfile == currentProfile) {
            return null
        }

        return "Invalid profile, current profile: $currentProfile, service profile: $serviceProfile"
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(WikiCommandProfileChecker::class, fail = false)
annotation class WikiCommandProfile(@get:JvmName("value") val profile: Profile) {
    enum class Profile {
        JAVA,
        KOTLIN,
        KOTLIN_DSL
    }
}
