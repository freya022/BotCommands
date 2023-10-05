package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.core.BContext
import com.freya02.botcommands.api.core.service.CustomConditionChecker
import com.freya02.botcommands.test_kt.services.annotations.RequireProfile

object ProfileChecker : CustomConditionChecker<RequireProfile> {
    // Let's just say it's loaded from the config
    private val currentProfile = Profile.DEV

    override val annotationType = RequireProfile::class.java

    override fun checkServiceAvailability(
        context: BContext,
        checkedClass: Class<*>,
        annotation: RequireProfile
    ): String? {
        if (annotation.profile != currentProfile) {
            return "Profile ${annotation.profile} is required, current profile is $currentProfile"
        }

        return null
    }
}