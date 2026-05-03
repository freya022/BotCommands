package io.github.freya022.botcommands.api.emojis.annotations

import io.github.freya022.botcommands.api.core.config.BAppEmojisConfig
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.emojis.RequiresAppEmojisChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

/**
 * Prevents usage of the annotated service if the application emojis feature is not registered.
 *
 * @see BAppEmojisConfig
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Condition(RequiresAppEmojisChecker::class)
@ConditionalOnProperty("botcommands.app.emojis.enable")
annotation class RequiresAppEmojis
