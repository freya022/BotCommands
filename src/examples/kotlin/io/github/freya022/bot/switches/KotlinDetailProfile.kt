package io.github.freya022.bot.switches

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.annotations.Condition

/**
 * Annotation condition to switch between Kotlin DSL examples and annotated Kotlin.
 */
object KotlinDetailProfileChecker : CustomConditionChecker<KotlinDetailProfile> {
    private val currentProfile = KotlinDetailProfile.Profile.KOTLIN_DSL

    override val annotationType: Class<KotlinDetailProfile> = KotlinDetailProfile::class.java

    override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>, annotation: KotlinDetailProfile): String? {
        val serviceProfile = annotation.profile
        if (serviceProfile == currentProfile) {
            return null
        }

        return "Invalid profile, current profile: $currentProfile, service profile: $serviceProfile"
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(KotlinDetailProfileChecker::class, fail = false)
annotation class KotlinDetailProfile(@get:JvmName("value") val profile: Profile) {
    enum class Profile {
        KOTLIN,
        KOTLIN_DSL
    }
}
