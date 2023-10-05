package io.github.freya022.botcommands.test_kt.services.annotations

import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.test_kt.services.Profile
import io.github.freya022.botcommands.test_kt.services.ProfileChecker

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(ProfileChecker::class, fail = false)
annotation class RequireProfile(val profile: Profile)
