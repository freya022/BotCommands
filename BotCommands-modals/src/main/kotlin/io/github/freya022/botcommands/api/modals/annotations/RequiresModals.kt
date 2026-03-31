package io.github.freya022.botcommands.api.modals.annotations

import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.internal.modals.RequiresModalsChecker
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

/**
 * Prevents usage of the annotated service if modals are not enabled.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.ANNOTATION_CLASS)
@Condition(RequiresModalsChecker::class)
@ConditionalOnProperty("botcommands.modals.enable", matchIfMissing = true)
annotation class RequiresModals
