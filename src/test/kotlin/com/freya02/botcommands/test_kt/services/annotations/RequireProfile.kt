package com.freya02.botcommands.test_kt.services.annotations

import com.freya02.botcommands.api.core.service.annotations.Condition
import com.freya02.botcommands.test_kt.services.Profile
import com.freya02.botcommands.test_kt.services.ProfileChecker

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(ProfileChecker::class, fail = false)
annotation class RequireProfile(val profile: Profile)
