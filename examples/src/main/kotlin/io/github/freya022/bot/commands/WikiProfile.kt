package io.github.freya022.bot.commands

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.CustomConditionChecker
import com.freya02.botcommands.api.core.service.annotations.Condition

object ProfileChecker : CustomConditionChecker<WikiProfile> {
    // NOTE: When changing wiki source code, the wiki downloads snippets at build time,
    // and must be rebuilt for the changes to be taken into account
    private val currentProfile = WikiProfile.Profile.KOTLIN_DSL

    override val annotationType: Class<WikiProfile> = WikiProfile::class.java

    override fun checkServiceAvailability(context: BContext, checkedClass: Class<*>, annotation: WikiProfile): String? {
        val serviceProfile = annotation.profile
        if (serviceProfile == currentProfile) {
            return null
        }

        return "Invalid profile, current profile: $currentProfile, service profile: $serviceProfile"
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(ProfileChecker::class, fail = false)
annotation class WikiProfile(@get:JvmName("value") val profile: Profile) {
    enum class Profile {
        JAVA,
        KOTLIN,
        KOTLIN_DSL
    }
}
