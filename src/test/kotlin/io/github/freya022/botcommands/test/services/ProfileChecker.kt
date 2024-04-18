package io.github.freya022.botcommands.test.services

import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.test.services.annotations.RequireProfile

object ProfileChecker : CustomConditionChecker<RequireProfile> {
    // Let's just say it's loaded from the config
    private val currentProfile = Profile.DEV

    override val annotationType = RequireProfile::class.java

    override fun checkServiceAvailability(
        serviceContainer: ServiceContainer,
        checkedClass: Class<*>,
        annotation: RequireProfile
    ): String? {
        if (annotation.profile != currentProfile) {
            return "Profile ${annotation.profile} is required, current profile is $currentProfile"
        }

        return null
    }
}