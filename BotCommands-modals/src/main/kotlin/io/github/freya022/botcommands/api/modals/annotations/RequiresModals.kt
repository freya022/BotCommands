package io.github.freya022.botcommands.api.modals.annotations

import io.github.freya022.botcommands.api.core.config.BModalsConfig
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.modals.RequiresModalsChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

/**
 * Prevents usage of the annotated service if the modals feature is not registered.
 *
 * @see BModalsConfig
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Condition(RequiresModalsChecker::class)
@ConditionalOnProperty("botcommands.modals.enable", matchIfMissing = true)
annotation class RequiresModals
