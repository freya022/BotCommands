package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.core.config.BComponentsConfig
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.components.RequiresComponentsChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

/**
 * Prevents usage of the annotated service if the components feature is not registered.
 *
 * @see BComponentsConfig
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Condition(RequiresComponentsChecker::class)
@ConditionalOnProperty("botcommands.components.enable", matchIfMissing = true)
@RequiresDatabase
annotation class RequiresComponents
