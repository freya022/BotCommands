package com.freya02.botcommands.api.core.service.annotations

import com.freya02.botcommands.api.core.config.BConfigBuilder
import com.freya02.botcommands.api.core.service.CustomConditionChecker
import kotlin.reflect.KClass

/**
 * Meta-annotation for [custom conditions][CustomConditionChecker].
 *
 * The annotated annotation need to be in the [search path][BConfigBuilder.addSearchPath]
 *
 * @see BService @BService
 * @see CustomConditionChecker
 */
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class Condition(
    /**
     * The implementation type of this condition
     */
    val type: KClass<out CustomConditionChecker<*>>,
    /**
     * Whether the service creation should throw when the condition isn't met.
     *
     * Default: `false`
     */
    val fail: Boolean = false
)