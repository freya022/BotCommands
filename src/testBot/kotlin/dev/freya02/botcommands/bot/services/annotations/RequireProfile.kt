package dev.freya02.botcommands.bot.services.annotations

import dev.freya02.botcommands.bot.services.Profile
import dev.freya02.botcommands.bot.services.ProfileChecker
import io.github.freya022.botcommands.api.core.service.annotations.Condition

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Condition(ProfileChecker::class, fail = false)
annotation class RequireProfile(val profile: Profile)
