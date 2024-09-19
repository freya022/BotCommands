package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.core.conditions.RequiredIntents
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.internal.core.service.annotations.HardcodedCondition
import kotlin.reflect.KClass

/**
 * Meta-annotation for [custom conditions][CustomConditionChecker].
 *
 * **Note:** This annotation needs to be **directly** used.
 *
 * Example:
 * ```kt
 * @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
 * @Condition(ProfileChecker::class, fail = false)
 * annotation class RequireProfile(val profile: Profile)
 * ```
 *
 * @see BService @BService
 * @see CustomConditionChecker
 *
 * @see RequiredIntents @RequiredIntents
 */
@MustBeDocumented
@HardcodedCondition
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class Condition(
    /**
     * The implementation type of this condition, same as [CustomConditionChecker.annotationType].
     */
    val type: KClass<out CustomConditionChecker<*>>,
    /**
     * Whether the service creation should throw when the condition isn't met.
     *
     * Default: `false`
     */
    val fail: Boolean = false
)