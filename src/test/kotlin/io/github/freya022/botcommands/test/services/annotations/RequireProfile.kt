package io.github.freya022.botcommands.test.services.annotations

import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.test.services.Profile
import io.github.freya022.botcommands.test.services.ProfileChecker

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(ProfileChecker::class, fail = false)
annotation class RequireProfile(val profile: Profile)
