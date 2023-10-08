package io.github.freya022.botcommands.api.core.service.annotations

import io.github.freya022.botcommands.api.core.conditions.RequiredIntents
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import kotlin.reflect.KClass

/**
 * Meta-annotation for [custom conditions][CustomConditionChecker].
 *
 * The annotated annotation need to be in the [search path][BConfigBuilder.addSearchPath]
 *
 * Example:
 * ```kt
 * @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
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