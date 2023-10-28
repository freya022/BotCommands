package io.github.freya022.bot.switches

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.annotations.Condition

object WikiDetailProfileChecker : CustomConditionChecker<WikiDetailProfile> {
    // NOTE: When changing wiki source code, the wiki downloads snippets at build time,
    // and must be rebuilt for the changes to be taken into account
    private val currentProfile = WikiDetailProfile.Profile.SIMPLIFIED

    override val annotationType: Class<WikiDetailProfile> = WikiDetailProfile::class.java

    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>, annotation: WikiDetailProfile): String? {
        val serviceProfile = annotation.profile
        if (serviceProfile == currentProfile) {
            return null
        }

        return "Invalid profile, current profile: $currentProfile, service profile: $serviceProfile"
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(WikiDetailProfileChecker::class, fail = false)
annotation class WikiDetailProfile(@get:JvmName("value") val profile: Profile) {
    enum class Profile {
        SIMPLIFIED,
        DETAILED
    }
}
